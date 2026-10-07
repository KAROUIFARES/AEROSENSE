# 🛰️ AeroSense - Firmware ESP32 (Station Météo IoT)

Ce sous-dossier contient le code source et la configuration PlatformIO du firmware embarqué sur le microcontrôleur **ESP32** pour la station météo connectée **AeroSense**.

---

## 📋 Table des Matières

1. [Vue d'ensemble](#-vue-densemble)
2. [Composants Matériels](#-composants-matériels)
3. [Schéma de Raccordement (Brochage / Pinout)](#-schéma-de-raccordement-brochage--pinout)
4. [Architecture Logicielle](#-architecture-logicielle)
5. [Configuration Réseau & MQTT](#-configuration-réseau--mqtt)
6. [Format des Données (Payload JSON)](#-format-des-données-payload-json)
7. [Compilation et Téléversement](#-compilation-et-téléversement)
8. [Surveillance & Débogage](#-surveillance--débogage)

---

## 🔍 Vue d'ensemble

Le firmware réalise les opérations suivantes :
- **Acquisition périodique** des mesures de température (°C) et d'humidité relative (%) via le capteur **DHT11**.
- **Affichage local temps réel** sur un écran **LCD 1602** (16 colonnes × 2 lignes) contrôlé en mode parallèle 4 bits.
- **Connexion Wi-Fi (802.11 b/g/n)** en mode station (STA).
- **Communication MQTTS sécurisée (TLS/SSL)** vers un broker Cloud (EMQX / HiveMQ Cloud sur le port 8883).
- **Publication automatique** de la télémétrie sous forme d'un objet JSON envoyé toutes les 5 secondes.

---

## 🔌 Composants Matériels

| Composant | Description | Rôle |
| :--- | :--- | :--- |
| **ESP32-WROOM-32** (NodeMCU-32S / ESP32 DevKit) | Microcontrôleur double cœur 240 MHz avec Wi-Fi & Bluetooth | Cœur de traitement et passerelle IoT |
| **DHT11** | Capteur d'ambiance numérique | Mesure de température et humidité relative |
| **LCD 1602** | Afficheur à cristaux liquides (contrôleur HD44780) | Affichage local des données et statuts |
| **Potentiomètre 10kΩ** | Potentiomètre rotatif | Ajustement du contraste du LCD (broche V0) |
| **Résistance de pull-up 4.7kΩ / 10kΩ** | Résistance de rappel | Ligne DATA du capteur DHT11 |

---

## 📌 Schéma de Raccordement (Brochage / Pinout)

### 1. Capteur DHT11
| Broche DHT11 | Broche ESP32 | Description |
| :--- | :--- | :--- |
| **VCC** | 3.3V / 5V | Alimentation capteur |
| **DATA** | **GPIO 21** | Signal de données (One-Wire) |
| **GND** | GND | Masse commune |

### 2. Afficheur LCD 1602 (Mode 4 bits)
| Broche LCD 1602 | Broche ESP32 / Circuit | Description |
| :--- | :--- | :--- |
| **Pin 1 (VSS)** | GND | Masse |
| **Pin 2 (VDD)** | 5V | Alimentation logique |
| **Pin 3 (V0)** | Curseur potentiomètre | Réglage du contraste |
| **Pin 4 (RS)** | **GPIO 27** | Register Select |
| **Pin 5 (RW)** | GND | Mode écriture (Read/Write mis à la masse) |
| **Pin 6 (E)** | **GPIO 26** | Enable (Validation) |
| **Pin 11 (D4)** | **GPIO 25** | Données bit 4 |
| **Pin 12 (D5)** | **GPIO 33** | Données bit 5 |
| **Pin 13 (D6)** | **GPIO 32** | Données bit 6 |
| **Pin 14 (D7)** | **GPIO 14** | Données bit 7 |
| **Pin 15 (A)** | 5V (via résistance 220Ω) | Rétroéclairage Anode (+) |
| **Pin 16 (K)** | GND | Rétroéclairage Cathode (-) |

---

## 🏗️ Architecture Logicielle

Le projet est structuré selon les standards **PlatformIO** :

```text
Code_ESP32/StationMeteo/
├── platformio.ini       # Configuration du projet, de la cible et des dépendances
├── include/             # Fichiers d'en-tête additionnels
├── lib/                 # Bibliothèques privées du projet
├── src/
│   ├── config.h         # Constantes de configuration Wi-Fi et broker MQTT
│   └── main.cpp         # Logique principale (Setup, Loop, MQTT, DHT, LCD)
└── test/                # Tests unitaires
```

### Dépendances (`platformio.ini`) :
- `adafruit/DHT sensor library@^1.4.7` : Pilote de communication avec le capteur DHT11.
- `arduino-libraries/LiquidCrystal@^1.0.7` : Gestion de l'écran LCD 1602 (standard HD44780).
- `knolleary/PubSubClient@^2.8` : Client MQTT léger pour microcontrôleurs.

---

## ⚙️ Configuration Réseau & MQTT

Toutes les informations d'identification se trouvent dans `src/config.h` :

```cpp
// --- Configuration Wi-Fi ---
#define WIFI_SSID     "VOTRE_SSID_WIFI"
#define WIFI_PASSWORD "VOTRE_MOT_DE_PASSE"

// --- Configuration Broker Cloud ---
#define MQTT_BROKER   "h1211a42.ala.eu-central-1.emqxsl.com" // Hôte EMQX / HiveMQ
#define MQTT_PORT     8883                                   // Port MQTTS chiffré TLS
#define MQTT_USER     "KAROUI"                               // Identifiant MQTT
#define MQTT_PASS     "aerosense"                            // Mot de passe MQTT

// --- Topics ---
#define MQTT_TOPIC_DATA "aerosense/data"                     // Topic de télémétrie
```

> **Sécurité TLS :** La couche réseau utilise `WiFiClientSecure` avec l'option `espClient.setInsecure()` pour dialoguer de façon chiffrée (SSL/TLS sur le port 8883) avec le broker sans nécessiter d'embarquer les certificats racines volumineux.

---

## 📦 Format des Données (Payload JSON)

Le firmware encapsule les mesures et les transmet sur le sujet MQTT `aerosense/data` :

```json
{
  "temp": 24.5,
  "hum": 60.0
}
```

- `temp` : Température mesurée en degrés Celsius (°C), arrondi à 1 décimale.
- `hum` : Humidité relative mesurée en pourcentage (%), arrondi à 1 décimale.

Ce format est directement décodé en temps réel par l'application Android **AeroSense Dashboard**.

---

## 🚀 Compilation et Téléversement

### Prérequis
- [Visual Studio Code](https://code.visualstudio.com/) avec l'extension **PlatformIO IDE**, ou la CLI PlatformIO (`pip install platformio`).

### Commandes en ligne de commande (CLI)

1. **Ouvrir le dossier :**
   ```bash
   cd Code_ESP32/StationMeteo
   ```

2. **Compiler le firmware :**
   ```bash
   pio run
   ```

3. **Téléverser sur l'ESP32 :**
   ```bash
   pio run --target upload
   ```

4. **Ouvrir le moniteur série (115200 bauds) :**
   ```bash
   pio device monitor -b 115200
   ```

---

## 🖥️ Surveillance & Débogage

Au démarrage, le moniteur série affiche le statut complet de la station :
```text
[AeroSense] Démarrage du système...
[AeroSense] Capteur DHT11 initialisé.
[AeroSense] Connexion au Wi-Fi : Topnet_12P8
.....
[AeroSense] Wi-Fi connecté !
[AeroSense] Adresse IP locale : 192.168.1.55
[AeroSense] Configuration MQTT appliquée.
[AeroSense] Tentative de connexion au broker MQTT... Connecté !
[AeroSense] Température: 24.2 °C | Humidité: 58.0 %
[AeroSense] Données publiées avec succès sur MQTT.
```
