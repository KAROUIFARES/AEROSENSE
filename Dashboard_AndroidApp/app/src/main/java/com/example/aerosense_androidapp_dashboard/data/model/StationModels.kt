package com.example.aerosense_androidapp_dashboard.data.model

import kotlin.math.ln
import kotlin.math.roundToInt

data class MqttConfig(
    val brokerHost: String = "h1211a42.ala.eu-central-1.emqxsl.com",
    val brokerPort: Int = 8883,
    val clientId: String = "AeroSense_Android_" + java.util.UUID.randomUUID().toString().take(8),
    val username: String = "KAROUI",
    val password: String = "aerosense",
    val tempTopic: String = "aerosense/temperature",
    val humTopic: String = "aerosense/humidity",
    val unifiedTopic: String = "aerosense/data",
    val commandTopic: String = "aerosense/commands",
    val useSsl: Boolean = true
) {
    val serverUri: String
        get() {
            val protocol = if (useSsl) "ssl://" else "tcp://"
            return "$protocol$brokerHost:$brokerPort"
        }
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class AirComfort(val label: String, val description: String) {
    IDEAL("Confort Optimal", "Température et humidité bien équilibrées"),
    DRY("Air Trop Sec", "Humidité basse, risque d'irritation"),
    HUMID("Air Trop Humide", "Humidité élevée, risque de moisissure"),
    COLD("Environnement Froid", "Température inférieure au seuil de confort"),
    HOT("Environnement Chaud", "Température élevée"),
    UNKNOWN("En attente...", "Mesure en cours d'acquisition")
}

data class StationData(
    val temperature: Float? = null,
    val humidity: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val dewPoint: Float?
        get() {
            if (temperature == null || humidity == null || humidity <= 0f) return null
            val a = 17.27f
            val b = 237.7f
            val alpha = ((a * temperature) / (b + temperature)) + ln(humidity / 100.0).toFloat()
            val dp = (b * alpha) / (a - alpha)
            return (dp * 10f).roundToInt() / 10f
        }

    val heatIndex: Float?
        get() {
            if (temperature == null || humidity == null) return null
            // Simplified formula for heat index
            val t = temperature
            val r = humidity
            val hi = t + 0.33f * (r / 100f * 6.105f * kotlin.math.exp(17.27f * t / (237.7f + t))) - 4.0f
            return (hi * 10f).roundToInt() / 10f
        }

    val comfortStatus: AirComfort
        get() {
            if (temperature == null || humidity == null) return AirComfort.UNKNOWN
            return when {
                temperature < 17f -> AirComfort.COLD
                temperature > 28f -> AirComfort.HOT
                humidity < 35f -> AirComfort.DRY
                humidity > 68f -> AirComfort.HUMID
                else -> AirComfort.IDEAL
            }
        }
}

data class AlertThresholds(
    val tempMax: Float = 30.0f,
    val tempMin: Float = 16.0f,
    val humMax: Float = 70.0f,
    val humMin: Float = 30.0f,
    val isTempAlertEnabled: Boolean = true,
    val isHumAlertEnabled: Boolean = true
)

data class ActiveAlert(
    val title: String,
    val message: String,
    val isCritical: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class HistoryPoint(
    val timestamp: Long,
    val temperature: Float,
    val humidity: Float
)
