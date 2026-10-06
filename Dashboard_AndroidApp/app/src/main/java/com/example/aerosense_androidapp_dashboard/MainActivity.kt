package com.example.aerosense_androidapp_dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aerosense_androidapp_dashboard.ui.screens.DashboardScreen
import com.example.aerosense_androidapp_dashboard.ui.theme.AeroSense_AndroidAPP_DashboardTheme
import com.example.aerosense_androidapp_dashboard.ui.viewmodel.AeroSenseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AeroSense_AndroidAPP_DashboardTheme {
                val viewModel: AeroSenseViewModel = viewModel()
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}