/**
 * ============================================================================
 * @file       config.h
 * @brief      Fichier de configuration réseau et MQTT pour AeroSense ESP32
 * @project    AeroSense - Station Météo IoT
 * @details    Ce fichier centralise les identifiants de connexion Wi-Fi ainsi
 *             que les paramètres d'accès au broker MQTT sécurisé (Cloud).
 * 
 * @attention  Veillez à ne pas partager publiquement vos clés et mots de passe
 *             en environnement de production.
 * ============================================================================
 */

#ifndef CONFIG_H
#define CONFIG_H

// ============================================================================
// 1. CONFIGURATION DU RÉSEAU WI-FI
// ============================================================================
/** @brief SSID (Nom) du point d'accès Wi-Fi */
#define WIFI_SSID     "Topnet_12P8"

/** @brief Mot de passe du réseau Wi-Fi */
#define WIFI_PASSWORD "123456789aa"


// ============================================================================
// 2. CONFIGURATION DU BROKER MQTT CLOUD (EMQX / HiveMQ)
// ============================================================================
/** @brief Adresse d'hôte (Host URL) du broker MQTT Cloud */
#define MQTT_BROKER   "h1211a42.ala.eu-central-1.emqxsl.com"

/** 
 * @brief Port d'écoute du broker MQTT
 * @note  8883 est le port standard pour les connexions chiffrées TLS/SSL (MQTTS).
 *        Pour du TCP standard non chiffré, le port est généralement 1883.
 */
#define MQTT_PORT     8883

/** @brief Nom d'utilisateur pour l'authentification MQTT */
#define MQTT_USER     "KAROUI"

/** @brief Mot de passe associé pour l'authentification MQTT */
#define MQTT_PASS     "aerosense"


// ============================================================================
// 3. SUJETS (TOPICS) MQTT
// ============================================================================
/** 
 * @brief Topic de publication des données télémétriques
 * @details Format du message envoyé (JSON) :
 *          {"temp": 24.5, "hum": 60.2}
 */
#define MQTT_TOPIC_DATA "aerosense/data"

#endif // CONFIG_H