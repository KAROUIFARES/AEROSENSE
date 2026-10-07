/**
 * ============================================================================
 * @file       main.cpp
 * @brief      Firmware ESP32 pour la Station Météo Connectée AeroSense
 * @project    AeroSense - Station Météo IoT
 * @details    Ce programme réalise :
 *             1. L'initialisation du matériel (Afficheur LCD 1602 et Capteur DHT11).
 *             2. La connexion au point d'accès Wi-Fi local.
 *             3. La connexion sécurisée en TLS/SSL au broker Cloud MQTT.
 *             4. La lecture périodique des mesures de température et d'humidité.
 *             5. L'encapsulation des mesures au format JSON et publication MQTT.
 *             6. L'affichage local et continu des grandeurs sur l'écran LCD.
 * ============================================================================
 */

#include <Arduino.h>
#include <DHT.h>
#include <LiquidCrystal.h>

#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>

#include "config.h"

// ============================================================================
// DÉFINITIONS MATÉRIELLES & BROCHAGE (PINOUT)
// ============================================================================

/** @brief Broche GPIO connectée à la ligne DATA du capteur DHT11 */
const int DHTPIN = 21;

/** @brief Type de capteur utilisé (DHT11, DHT22 ou DHT21) */
#define DHTTYPE DHT11

/**
 * @brief Instance du pilote pour le capteur d'ambiance DHT11
 * @param DHTPIN  GPIO 21 (Données avec résistance de pull-up)
 * @param DHTTYPE DHT11
 */
DHT dht(DHTPIN, DHTTYPE);

/**
 * @brief Configuration de l'afficheur LCD 16x2 en mode parallèle 4 bits
 * @param rs GPIO 27 (Register Select : choix commande ou donnée)
 * @param en GPIO 26 (Enable : impulsion de validation)
 * @param d4 GPIO 25 (Bus de données bit 4)
 * @param d5 GPIO 33 (Bus de données bit 5)
 * @param d6 GPIO 32 (Bus de données bit 6)
 * @param d7 GPIO 14 (Bus de données bit 7)
 */
LiquidCrystal lcd(27, 26, 25, 33, 32, 14);

// ============================================================================
// CLIENTS RÉSEAU & SÉCURITÉ TLS/MQTT
// ============================================================================

/** 
 * @brief Client Wi-Fi avec support de chiffrement TLS/SSL
 * @note  Permet d'établir une liaison sécurisée avec le port MQTTS 8883.
 */
WiFiClientSecure espClient;

/** 
 * @brief Gestionnaire de messages et protocole MQTT (PubSubClient)
 * @note  S'appuie sur le socket TLS fourni par espClient.
 */
PubSubClient client(espClient);

// ============================================================================
// PROTOTYPES DES FONCTIONS
// ============================================================================

/**
 * @brief Établit ou rétablit la connexion avec le broker MQTT Cloud.
 *        Boucle jusqu'à l'obtention d'une liaison active.
 */
void connectMQTT();

// ============================================================================
// INITIALISATION DU SYSTÈME (SETUP)
// ============================================================================

/**
 * @brief Initialisation des périphériques matériels et des interfaces réseau.
 *        Exécuté une seule fois à la mise sous tension ou au redémarrage.
 */
void setup()
{
  // 1. Initialisation de la communication série pour le débogage (115200 bauds)
  Serial.begin(115200);
  Serial.println("\n[AeroSense] Démarrage du système...");

  // 2. Initialisation de l'afficheur LCD (16 colonnes, 2 lignes)
  lcd.begin(16, 2);
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("AEROSENSE...");
  lcd.setCursor(0, 1);
  lcd.print("Initialisation");

  // 3. Initialisation du capteur DHT11
  dht.begin();
  Serial.println("[AeroSense] Capteur DHT11 initialisé.");

  // 4. Connexion au réseau Wi-Fi
  Serial.print("[AeroSense] Connexion au Wi-Fi : ");
  Serial.println(WIFI_SSID);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n[AeroSense] Wi-Fi connecté !");
  Serial.print("[AeroSense] Adresse IP locale : ");
  Serial.println(WiFi.localIP());

  // 5. Configuration de la couche de sécurité TLS
  // setInsecure désactive la validation stricte de la chaîne de certificats racine
  // ce qui est optimal pour les microcontrôleurs légers accédant au cloud.
  espClient.setInsecure();

  // 6. Configuration du serveur MQTT
  client.setServer(MQTT_BROKER, MQTT_PORT);
  Serial.println("[AeroSense] Configuration MQTT appliquée.");

  // 7. Message d'accueil temporaire sur l'écran LCD
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("AEROSENSE...");
  lcd.setCursor(0, 1);
  lcd.print("Station Prete !");
  delay(3000);
  lcd.clear();
}

// ============================================================================
// BOUCLE PRINCIPALE D'EXÉCUTION (LOOP)
// ============================================================================

/**
 * @brief Cycle principal du firmware :
 *        - Maintien du lien MQTT
 *        - Échantillonnage des données météo
 *        - Envoi télémétrique JSON
 *        - Mise à jour de l'affichage local
 */
void loop()
{
  // 1. Vérification et rétablissement de la liaison MQTT si nécessaire
  if (!client.connected())
  {
    connectMQTT();
  }
  client.loop(); // Traitement des événements réseau MQTT (keep-alive / ping)

  // 2. Acquisition des mesures physiques depuis le capteur DHT11
  float temperature = dht.readTemperature();
  float humidity = dht.readHumidity();

  // 3. Contrôle de validité des mesures reçues
  if (isnan(temperature) || isnan(humidity))
  {
    Serial.println("[AeroSense] Avertissement : Échec de lecture du capteur DHT11 !");
    lcd.setCursor(0, 0);
    lcd.print("Erreur DHT11    ");
  }
  else
  {
    // Affichage des informations sur le moniteur série
    Serial.printf("[AeroSense] Température: %.1f °C | Humidité: %.1f %%\n", temperature, humidity);

    // 4. Construction de la charge utile (Payload) au format JSON
    // Format : {"temp": 24.50, "hum": 60.00}
    String payload = "{\"temp\":" + String(temperature, 1) + ",\"hum\":" + String(humidity, 1) + "}";

    // 5. Publication de la télémétrie sur le topic MQTT configuré
    if (client.publish(MQTT_TOPIC_DATA, payload.c_str()))
    {
      Serial.println("[AeroSense] Données publiées avec succès sur MQTT.");
    }
    else
    {
      Serial.println("[AeroSense] Erreur lors de la publication MQTT.");
    }

    // 6. Mise à jour des indications sur l'afficheur LCD 1602
    // Ligne 1 : Température en °C
    lcd.setCursor(0, 0);
    lcd.print("Temp : ");
    lcd.print(temperature, 1);
    lcd.print(" \xDF" "C   "); // \xDF correspond au symbole degré '°' sur HD44780

    // Ligne 2 : Humidité relative en %
    lcd.setCursor(0, 1);
    lcd.print("Hum  : ");
    lcd.print(humidity, 1);
    lcd.print(" %    ");
  }

  // 7. Intervalle d'attente avant la prochaine mesure (5 secondes)
  delay(5000);
}

// ============================================================================
// GESTIONNAIRE DE CONNEXION MQTT
// ============================================================================

/**
 * @brief Assure la négociation de session avec le broker MQTT Cloud.
 *        Réitère la tentative toutes les 2 secondes en cas d'échec.
 */
void connectMQTT()
{
  while (!client.connected())
  {
    Serial.print("[AeroSense] Tentative de connexion au broker MQTT...");
    String clientId = "ESP32_AeroSense";

    // Tentative de connexion avec identification et mot de passe
    if (client.connect(clientId.c_str(), MQTT_USER, MQTT_PASS))
    {
      Serial.println(" Connecté !");
      lcd.setCursor(0, 1);
      lcd.print("Broker Connecte!");
      delay(1000);
      lcd.clear();
    }
    else
    {
      Serial.printf(" Échec (rc = %d). Nouvelle tentative dans 2s...\n", client.state());
      lcd.setCursor(0, 1);
      lcd.print("Attente MQTT... ");
      delay(2000);
    }
  }
}