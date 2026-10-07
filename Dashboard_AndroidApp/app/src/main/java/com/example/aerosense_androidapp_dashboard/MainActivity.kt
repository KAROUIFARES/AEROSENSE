package com.example.aerosense_androidapp_dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aerosense_androidapp_dashboard.ui.screens.DashboardScreen
import com.example.aerosense_androidapp_dashboard.ui.screens.SplashScreen
import com.example.aerosense_androidapp_dashboard.ui.theme.AeroSense_AndroidAPP_DashboardTheme
import com.example.aerosense_androidapp_dashboard.ui.viewmodel.AeroSenseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AeroSense_AndroidAPP_DashboardTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }

                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                    },
                    label = "ScreenTransition"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            onTimeout = { showSplash = false }
                        )
                    } else {
                        val viewModel: AeroSenseViewModel = viewModel()
                        DashboardScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}