package com.example.aerosense_androidapp_dashboard.data.mqtt

import android.util.Log
import com.example.aerosense_androidapp_dashboard.data.model.ConnectionStatus
import com.example.aerosense_androidapp_dashboard.data.model.MqttConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        private const val TAG = "AeroSense_MQTT"
    }

    private var client: MqttAsyncClient? = null
    private var currentConfig: MqttConfig = MqttConfig()

    private val _connectionState = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _statusMessage = MutableStateFlow<String>("Déconnecté")
    val statusMessage = _statusMessage.asStateFlow()

    private val _messageFlow = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 64)
    val messageFlow = _messageFlow.asSharedFlow()

    fun connect(config: MqttConfig) {
        currentConfig = config
        scope.launch(Dispatchers.IO) {
            try {
                disconnectInternal()
                _connectionState.value = ConnectionStatus.CONNECTING
                _statusMessage.value = "Connexion à ${config.brokerHost}..."

                val newClient = MqttAsyncClient(
                    config.serverUri,
                    config.clientId,
                    MemoryPersistence()
                )
                client = newClient

                newClient.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                        Log.d(TAG, "Connect complete. Reconnect: $reconnect")
                        _connectionState.value = ConnectionStatus.CONNECTED
                        _statusMessage.value = if (reconnect) "Reconnecté au broker" else "Connecté au broker"
                        subscribeToTopics(listOf(config.tempTopic, config.humTopic, config.unifiedTopic))
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.w(TAG, "Connection lost: ${cause?.message}")
                        _connectionState.value = ConnectionStatus.DISCONNECTED
                        _statusMessage.value = "Connexion perdue : ${cause?.localizedMessage ?: "Inconnue"}"
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        if (topic != null && message != null) {
                            val payload = String(message.payload)
                            Log.d(TAG, "Msg received [$topic]: $payload")
                            scope.launch {
                                _messageFlow.emit(topic to payload)
                            }
                        }
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        Log.d(TAG, "Delivery complete")
                    }
                })

                val options = MqttConnectOptions().apply {
                    isAutomaticReconnect = true
                    isCleanSession = true
                    connectionTimeout = 10
                    keepAliveInterval = 20
                    if (config.username.isNotBlank()) {
                        userName = config.username
                    }
                    if (config.password.isNotBlank()) {
                        password = config.password.toCharArray()
                    }
                }

                newClient.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(TAG, "Mqtt connected successfully")
                        _connectionState.value = ConnectionStatus.CONNECTED
                        _statusMessage.value = "Connecté à ${config.brokerHost}"
                        subscribeToTopics(listOf(config.tempTopic, config.humTopic, config.unifiedTopic))
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e(TAG, "Mqtt connection failed: ${exception?.message}")
                        _connectionState.value = ConnectionStatus.ERROR
                        _statusMessage.value = "Échec : ${exception?.localizedMessage ?: "Erreur de connexion"}"
                    }
                })

            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to MQTT", e)
                _connectionState.value = ConnectionStatus.ERROR
                _statusMessage.value = "Erreur : ${e.localizedMessage ?: e.message}"
            }
        }
    }

    private fun subscribeToTopics(topics: List<String>) {
        val current = client ?: return
        if (!current.isConnected) return

        try {
            topics.forEach { topic ->
                if (topic.isNotBlank()) {
                    current.subscribe(topic, 0, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(TAG, "Souscrit au topic : $topic")
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.w(TAG, "Échec souscription : $topic (${exception?.message})")
                        }
                    })
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la souscription aux topics", e)
        }
    }

    fun publish(topic: String, payload: String, qos: Int = 1) {
        scope.launch(Dispatchers.IO) {
            val current = client
            if (current != null && current.isConnected) {
                try {
                    val msg = MqttMessage(payload.toByteArray()).apply {
                        this.qos = qos
                    }
                    current.publish(topic, msg)
                    Log.d(TAG, "Publié sur $topic: $payload")
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur publication", e)
                }
            } else {
                Log.w(TAG, "Impossible de publier : non connecté")
            }
        }
    }

    fun disconnect() {
        scope.launch(Dispatchers.IO) {
            disconnectInternal()
            _connectionState.value = ConnectionStatus.DISCONNECTED
            _statusMessage.value = "Déconnecté"
        }
    }

    private fun disconnectInternal() {
        try {
            if (client?.isConnected == true) {
                client?.disconnect(500)
            }
            client?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Disconnect warning: ${e.message}")
        } finally {
            client = null
        }
    }
}
