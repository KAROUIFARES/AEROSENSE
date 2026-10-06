package com.example.aerosense_androidapp_dashboard.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Colors
val AeroPrimary = Color(0xFF10B981)        // Emerald Green (Clean Air)
val AeroPrimaryVariant = Color(0xFF059669)
val AeroSecondary = Color(0xFF0EA5E9)      // Sky Blue (Atmosphere)
val AeroAccent = Color(0xFF6366F1)         // Indigo

// Environmental Status Colors
val TempCold = Color(0xFF38BDF8)           // Ice Blue
val TempComfort = Color(0xFF10B981)        // Ideal Green
val TempWarm = Color(0xFFF59E0B)           // Warm Amber
val TempHot = Color(0xFFEF4444)            // Coral Alert

val HumDry = Color(0xFFF59E0B)             // Dry Amber
val HumIdeal = Color(0xFF06B6D4)           // Cyan Ideal
val HumHigh = Color(0xFF8B5CF6)            // Purple / High Humidity

// Dark Backgrounds & Surfaces (Glassmorphic & Sleek)
val DarkBackground = Color(0xFF0B132B)     // Deep Midnight
val DarkSurface = Color(0xFF1C2541)        // Rich Slate Surface
val DarkCardBg = Color(0xFF1E293B)         // Card Navy
val DarkCardBorder = Color(0xFF334155)     // Subtle Border
val DarkTextPrimary = Color(0xFFF8FAFC)    // Crisp White
val DarkTextSecondary = Color(0xFF94A3B8)  // Muted Slate

// Light Backgrounds & Surfaces
val LightBackground = Color(0xFFF1F5F9)    // Off-white slate
val LightSurface = Color(0xFFFFFFFF)       // Pure White
val LightCardBg = Color(0xFFFFFFFF)
val LightCardBorder = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)

// Connection Status Colors
val StatusConnected = Color(0xFF10B981)
val StatusConnecting = Color(0xFFF59E0B)
val StatusDisconnected = Color(0xFF94A3B8)
val StatusError = Color(0xFFEF4444)