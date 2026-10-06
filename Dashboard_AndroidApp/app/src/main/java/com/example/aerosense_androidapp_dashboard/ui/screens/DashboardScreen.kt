package com.example.aerosense_androidapp_dashboard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aerosense_androidapp_dashboard.ui.components.AlertsBanner
import com.example.aerosense_androidapp_dashboard.ui.components.ComfortIndexCard
import com.example.aerosense_androidapp_dashboard.ui.components.MetricGaugeCard
import com.example.aerosense_androidapp_dashboard.ui.components.MetricType
import com.example.aerosense_androidapp_dashboard.ui.components.MqttConfigDialog
import com.example.aerosense_androidapp_dashboard.ui.components.RealtimeChartCard
import com.example.aerosense_androidapp_dashboard.ui.components.StationControlCard
import com.example.aerosense_androidapp_dashboard.ui.components.TopAppBarAeroSense
import com.example.aerosense_androidapp_dashboard.ui.viewmodel.AeroSenseViewModel

@Composable
fun DashboardScreen(
    viewModel: AeroSenseViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBarAeroSense(
                connectionStatus = state.connectionStatus,
                statusMessage = state.statusMessage,
                brokerHost = state.config.brokerHost,
                isDemoMode = state.isDemoMode,
                onToggleDemo = { viewModel.toggleDemoMode() },
                onOpenSettings = { viewModel.setSettingsOpen(true) },
                onReconnect = { viewModel.connect() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alerts Banner (if any)
            if (state.activeAlerts.isNotEmpty()) {
                item {
                    AlertsBanner(alerts = state.activeAlerts)
                }
            }

            // Temperature & Humidity Hero Gauges
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricGaugeCard(
                        type = MetricType.TEMPERATURE,
                        value = state.stationData.temperature,
                        minValue = state.minTemp,
                        maxValue = state.maxTemp
                    )

                    MetricGaugeCard(
                        type = MetricType.HUMIDITY,
                        value = state.stationData.humidity,
                        minValue = state.minHum,
                        maxValue = state.maxHum
                    )
                }
            }

            // Comfort Index & Dew Point
            item {
                ComfortIndexCard(data = state.stationData)
            }

            // Realtime Evolution Chart
            item {
                RealtimeChartCard(
                    history = state.history,
                    onClearHistory = { viewModel.clearHistory() }
                )
            }

            // Telemetry & ESP32 Remote Control
            item {
                StationControlCard(
                    totalPackets = state.totalPackets,
                    lastReceivedTime = state.lastReceivedTime,
                    onSendCommand = { cmd -> viewModel.sendCommand(cmd) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Settings & Configuration Dialog
    if (state.isSettingsOpen) {
        MqttConfigDialog(
            currentConfig = state.config,
            currentThresholds = state.thresholds,
            onSave = { newConfig, newThresholds ->
                viewModel.updateConfig(newConfig)
                viewModel.updateThresholds(newThresholds)
                viewModel.setSettingsOpen(false)
            },
            onDismiss = { viewModel.setSettingsOpen(false) }
        )
    }
}
