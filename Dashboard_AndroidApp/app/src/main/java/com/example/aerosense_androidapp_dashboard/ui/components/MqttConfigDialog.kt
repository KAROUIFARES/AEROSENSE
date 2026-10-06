package com.example.aerosense_androidapp_dashboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.aerosense_androidapp_dashboard.data.model.AlertThresholds
import com.example.aerosense_androidapp_dashboard.data.model.MqttConfig
import com.example.aerosense_androidapp_dashboard.ui.theme.AeroPrimary

@Composable
fun MqttConfigDialog(
    currentConfig: MqttConfig,
    currentThresholds: AlertThresholds,
    onSave: (MqttConfig, AlertThresholds) -> Unit,
    onDismiss: () -> Unit
) {
    var host by remember { mutableStateOf(currentConfig.brokerHost) }
    var port by remember { mutableStateOf(currentConfig.brokerPort.toString()) }
    var clientId by remember { mutableStateOf(currentConfig.clientId) }
    var username by remember { mutableStateOf(currentConfig.username) }
    var password by remember { mutableStateOf(currentConfig.password) }

    var tempTopic by remember { mutableStateOf(currentConfig.tempTopic) }
    var humTopic by remember { mutableStateOf(currentConfig.humTopic) }
    var unifiedTopic by remember { mutableStateOf(currentConfig.unifiedTopic) }
    var commandTopic by remember { mutableStateOf(currentConfig.commandTopic) }

    var tempMax by remember { mutableStateOf(currentThresholds.tempMax.toString()) }
    var tempMin by remember { mutableStateOf(currentThresholds.tempMin.toString()) }
    var humMax by remember { mutableStateOf(currentThresholds.humMax.toString()) }
    var humMin by remember { mutableStateOf(currentThresholds.humMin.toString()) }
    var isTempAlertEnabled by remember { mutableStateOf(currentThresholds.isTempAlertEnabled) }
    var isHumAlertEnabled by remember { mutableStateOf(currentThresholds.isHumAlertEnabled) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = AeroPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Paramètres de la Station",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer"
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Section 1: MQTT Broker
                Text(
                    text = "Serveur MQTT (Broker)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AeroPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets
                Text(
                    text = "Préréglages rapides :",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = host == "broker.hivemq.com",
                        onClick = {
                            host = "broker.hivemq.com"
                            port = "1883"
                        },
                        label = { Text("HiveMQ") }
                    )
                    FilterChip(
                        selected = host == "broker.emqx.io",
                        onClick = {
                            host = "broker.emqx.io"
                            port = "1883"
                        },
                        label = { Text("EMQX") }
                    )
                    FilterChip(
                        selected = host == "test.mosquitto.org",
                        onClick = {
                            host = "test.mosquitto.org"
                            port = "1883"
                        },
                        label = { Text("Mosquitto") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Adresse Broker (IP ou Domaine)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.4f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = clientId,
                        onValueChange = { clientId = it },
                        label = { Text("Client ID") },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Utilisateur (opt.)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe (opt.)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                // Section 2: Topics
                Text(
                    text = "Topics MQTT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AeroPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = unifiedTopic,
                    onValueChange = { unifiedTopic = it },
                    label = { Text("Topic JSON Général") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tempTopic,
                    onValueChange = { tempTopic = it },
                    label = { Text("Topic Température Dédié") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = humTopic,
                    onValueChange = { humTopic = it },
                    label = { Text("Topic Humidité Dédié") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = commandTopic,
                    onValueChange = { commandTopic = it },
                    label = { Text("Topic Commandes ESP32") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                // Section 3: Thresholds
                Text(
                    text = "Seuils d'Alertes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AeroPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Activer alertes température")
                    Switch(
                        checked = isTempAlertEnabled,
                        onCheckedChange = { isTempAlertEnabled = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tempMin,
                        onValueChange = { tempMin = it },
                        label = { Text("Temp Min (°C)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        enabled = isTempAlertEnabled,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempMax,
                        onValueChange = { tempMax = it },
                        label = { Text("Temp Max (°C)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        enabled = isTempAlertEnabled,
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Activer alertes humidité")
                    Switch(
                        checked = isHumAlertEnabled,
                        onCheckedChange = { isHumAlertEnabled = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = humMin,
                        onValueChange = { humMin = it },
                        label = { Text("Hum Min (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        enabled = isHumAlertEnabled,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = humMax,
                        onValueChange = { humMax = it },
                        label = { Text("Hum Max (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        enabled = isHumAlertEnabled,
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                Button(
                    onClick = {
                        val parsedPort = port.toIntOrNull() ?: 1883
                        val newConfig = currentConfig.copy(
                            brokerHost = host.trim(),
                            brokerPort = parsedPort,
                            clientId = clientId.trim(),
                            username = username.trim(),
                            password = password.trim(),
                            tempTopic = tempTopic.trim(),
                            humTopic = humTopic.trim(),
                            unifiedTopic = unifiedTopic.trim(),
                            commandTopic = commandTopic.trim()
                        )
                        val newThresholds = AlertThresholds(
                            tempMax = tempMax.toFloatOrNull() ?: 30f,
                            tempMin = tempMin.toFloatOrNull() ?: 16f,
                            humMax = humMax.toFloatOrNull() ?: 70f,
                            humMin = humMin.toFloatOrNull() ?: 30f,
                            isTempAlertEnabled = isTempAlertEnabled,
                            isHumAlertEnabled = isHumAlertEnabled
                        )
                        onSave(newConfig, newThresholds)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AeroPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enregistrer et Reconnecter",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
