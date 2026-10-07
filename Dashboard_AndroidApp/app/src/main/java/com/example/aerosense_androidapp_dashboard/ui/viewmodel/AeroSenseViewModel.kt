package com.example.aerosense_androidapp_dashboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aerosense_androidapp_dashboard.data.model.ActiveAlert
import com.example.aerosense_androidapp_dashboard.data.model.AlertThresholds
import com.example.aerosense_androidapp_dashboard.data.model.ConnectionStatus
import com.example.aerosense_androidapp_dashboard.data.model.HistoryPoint
import com.example.aerosense_androidapp_dashboard.data.model.MqttConfig
import com.example.aerosense_androidapp_dashboard.data.model.StationData
import com.example.aerosense_androidapp_dashboard.data.mqtt.MqttManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.roundToInt
import kotlin.random.Random

data class AeroSenseUiState(
    val stationData: StationData = StationData(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val statusMessage: String = "Déconnecté",
    val history: List<HistoryPoint> = emptyList(),
    val config: MqttConfig = MqttConfig(),
    val thresholds: AlertThresholds = AlertThresholds(),
    val activeAlerts: List<ActiveAlert> = emptyList(),
    val isDemoMode: Boolean = false,
    val minTemp: Float? = null,
    val maxTemp: Float? = null,
    val minHum: Float? = null,
    val maxHum: Float? = null,
    val totalPackets: Int = 0,
    val lastReceivedTime: Long? = null,
    val isSettingsOpen: Boolean = false
)

class AeroSenseViewModel(
    private val mqttManager: MqttManager = MqttManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AeroSenseUiState())
    val uiState = _uiState.asStateFlow()

    private var demoJob: Job? = null

    init {
        // Observe connection state
        viewModelScope.launch {
            mqttManager.connectionState.collect { status ->
                _uiState.update { it.copy(connectionStatus = status) }
            }
        }

        // Observe status message
        viewModelScope.launch {
            mqttManager.statusMessage.collect { msg ->
                _uiState.update { it.copy(statusMessage = msg) }
            }
        }

        // Observe incoming messages
        viewModelScope.launch {
            mqttManager.messageFlow.collect { (topic, payload) ->
                handleIncomingMessage(topic, payload)
            }
        }

        // Auto-connect to default broker on launch
        connect()
    }

    fun connect() {
        if (_uiState.value.isDemoMode) {
            stopDemoMode()
        }
        mqttManager.connect(_uiState.value.config)
    }

    fun disconnect() {
        mqttManager.disconnect()
    }

    fun updateConfig(newConfig: MqttConfig) {
        _uiState.update { it.copy(config = newConfig) }
        // Reconnect with new config
        connect()
    }

    fun updateThresholds(newThresholds: AlertThresholds) {
        _uiState.update { it.copy(thresholds = newThresholds) }
        evaluateAlerts(_uiState.value.stationData.temperature, _uiState.value.stationData.humidity)
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = isOpen) }
    }

    fun toggleDemoMode() {
        val willEnable = !_uiState.value.isDemoMode
        if (willEnable) {
            disconnect()
            startDemoMode()
        } else {
            stopDemoMode()
            connect()
        }
    }

    private fun startDemoMode() {
        _uiState.update {
            it.copy(
                isDemoMode = true,
                connectionStatus = ConnectionStatus.CONNECTED,
                statusMessage = "Mode Démonstration Actif (Simulé)"
            )
        }
        demoJob?.cancel()
        demoJob = viewModelScope.launch {
            var simTemp = 24.2f
            var simHum = 58.0f
            while (true) {
                // Realistic fluctuations
                simTemp += (Random.nextFloat() - 0.48f) * 0.4f
                simTemp = (simTemp * 10f).roundToInt() / 10f
                if (simTemp < 18f) simTemp = 18.5f
                if (simTemp > 35f) simTemp = 34.5f

                simHum += (Random.nextFloat() - 0.49f) * 0.8f
                simHum = (simHum * 10f).roundToInt() / 10f
                if (simHum < 30f) simHum = 32.0f
                if (simHum > 85f) simHum = 83.0f

                onNewSensorReading(simTemp, simHum)
                delay(2500)
            }
        }
    }

    private fun stopDemoMode() {
        demoJob?.cancel()
        demoJob = null
        _uiState.update {
            it.copy(
                isDemoMode = false,
                connectionStatus = ConnectionStatus.DISCONNECTED,
                statusMessage = "Déconnecté"
            )
        }
    }

    private fun handleIncomingMessage(topic: String, payload: String) {
        val trimmed = payload.trim()
        val config = _uiState.value.config

        try {
            when (topic) {
                config.tempTopic -> {
                    val temp = parseNumeric(trimmed)
                    if (temp != null) {
                        val currentHum = _uiState.value.stationData.humidity
                        onNewSensorReading(temp, currentHum)
                    }
                }
                config.humTopic -> {
                    val hum = parseNumeric(trimmed)
                    if (hum != null) {
                        val currentTemp = _uiState.value.stationData.temperature
                        onNewSensorReading(currentTemp, hum)
                    }
                }
                config.unifiedTopic -> {
                    // JSON format: {"temp": 23.5, "hum": 60} ou {"temperature": 23.5, "humidity": 60}
                    val json = JSONObject(trimmed)
                    val temp = when {
                        json.has("temp") && !json.isNull("temp") -> {
                            val v = json.optDouble("temp", Double.NaN)
                            if (v.isNaN()) null else v.toFloat()
                        }
                        json.has("temperature") && !json.isNull("temperature") -> {
                            val v = json.optDouble("temperature", Double.NaN)
                            if (v.isNaN()) null else v.toFloat()
                        }
                        else -> null
                    }

                    val hum = when {
                        json.has("hum") && !json.isNull("hum") -> {
                            val v = json.optDouble("hum", Double.NaN)
                            if (v.isNaN()) null else v.toFloat()
                        }
                        json.has("humidity") && !json.isNull("humidity") -> {
                            val v = json.optDouble("humidity", Double.NaN)
                            if (v.isNaN()) null else v.toFloat()
                        }
                        else -> null
                    }

                    if (temp != null || hum != null) {
                        onNewSensorReading(
                            temp ?: _uiState.value.stationData.temperature,
                            hum ?: _uiState.value.stationData.humidity
                        )
                    }
                }
                else -> {
                    // Try parsing as JSON even if topic doesn't match exactly
                    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                        val json = JSONObject(trimmed)
                        val temp = when {
                            json.has("temp") && !json.isNull("temp") -> {
                                val v = json.optDouble("temp", Double.NaN)
                                if (v.isNaN()) null else v.toFloat()
                            }
                            json.has("temperature") && !json.isNull("temperature") -> {
                                val v = json.optDouble("temperature", Double.NaN)
                                if (v.isNaN()) null else v.toFloat()
                            }
                            else -> null
                        }

                        val hum = when {
                            json.has("hum") && !json.isNull("hum") -> {
                                val v = json.optDouble("hum", Double.NaN)
                                if (v.isNaN()) null else v.toFloat()
                            }
                            json.has("humidity") && !json.isNull("humidity") -> {
                                val v = json.optDouble("humidity", Double.NaN)
                                if (v.isNaN()) null else v.toFloat()
                            }
                            else -> null
                        }

                        if (temp != null || hum != null) {
                            onNewSensorReading(
                                temp ?: _uiState.value.stationData.temperature,
                                hum ?: _uiState.value.stationData.humidity
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Logging or fallback parsing
        }
    }

    private fun parseNumeric(value: String): Float? {
        val clean = value.replace("°C", "")
            .replace("%", "")
            .replace(",", ".")
            .trim()
        return clean.toFloatOrNull()
    }

    private fun onNewSensorReading(newTemp: Float?, newHum: Float?) {
        val now = System.currentTimeMillis()
        val current = _uiState.value

        val updatedMinTemp = when {
            newTemp == null -> current.minTemp
            current.minTemp == null -> newTemp
            else -> minOf(current.minTemp, newTemp)
        }
        val updatedMaxTemp = when {
            newTemp == null -> current.maxTemp
            current.maxTemp == null -> newTemp
            else -> maxOf(current.maxTemp, newTemp)
        }
        val updatedMinHum = when {
            newHum == null -> current.minHum
            current.minHum == null -> newHum
            else -> minOf(current.minHum, newHum)
        }
        val updatedMaxHum = when {
            newHum == null -> current.maxHum
            current.maxHum == null -> newHum
            else -> maxOf(current.maxHum, newHum)
        }

        val updatedHistory = if (newTemp != null && newHum != null) {
            (current.history + HistoryPoint(now, newTemp, newHum)).takeLast(40)
        } else {
            current.history
        }

        val newData = StationData(temperature = newTemp, humidity = newHum, timestamp = now)

        _uiState.update {
            it.copy(
                stationData = newData,
                history = updatedHistory,
                minTemp = updatedMinTemp,
                maxTemp = updatedMaxTemp,
                minHum = updatedMinHum,
                maxHum = updatedMaxHum,
                totalPackets = current.totalPackets + 1,
                lastReceivedTime = now
            )
        }

        evaluateAlerts(newTemp, newHum)
    }

    private fun evaluateAlerts(temp: Float?, hum: Float?) {
        val th = _uiState.value.thresholds
        val alerts = mutableListOf<ActiveAlert>()

        if (th.isTempAlertEnabled && temp != null) {
            if (temp > th.tempMax) {
                alerts.add(
                    ActiveAlert(
                        title = "Alerte Température Élevée",
                        message = "La température actuelle (${temp}°C) dépasse le seuil max défini (${th.tempMax}°C).",
                        isCritical = true
                    )
                )
            } else if (temp < th.tempMin) {
                alerts.add(
                    ActiveAlert(
                        title = "Alerte Température Basse",
                        message = "La température actuelle (${temp}°C) est sous le seuil min défini (${th.tempMin}°C).",
                        isCritical = false
                    )
                )
            }
        }

        if (th.isHumAlertEnabled && hum != null) {
            if (hum > th.humMax) {
                alerts.add(
                    ActiveAlert(
                        title = "Alerte Humidité Élevée",
                        message = "L'humidité ambiante (${hum}%) dépasse le seuil d'inconfort (${th.humMax}%).",
                        isCritical = true
                    )
                )
            } else if (hum < th.humMin) {
                alerts.add(
                    ActiveAlert(
                        title = "Alerte Humidité Faible",
                        message = "L'air est trop sec (${hum}%), sous le seuil min (${th.humMin}%).",
                        isCritical = false
                    )
                )
            }
        }

        _uiState.update { it.copy(activeAlerts = alerts) }
    }

    fun sendCommand(cmd: String) {
        val topic = _uiState.value.config.commandTopic
        val jsonPayload = "{\"command\":\"$cmd\",\"timestamp\":${System.currentTimeMillis()}}"
        mqttManager.publish(topic, jsonPayload)
    }

    fun clearHistory() {
        _uiState.update {
            it.copy(
                history = emptyList(),
                minTemp = it.stationData.temperature,
                maxTemp = it.stationData.temperature,
                minHum = it.stationData.humidity,
                maxHum = it.stationData.humidity,
                totalPackets = 0
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        demoJob?.cancel()
        mqttManager.disconnect()
    }
}
