package com.example.aerosense_androidapp_dashboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aerosense_androidapp_dashboard.ui.theme.DarkCardBorder
import com.example.aerosense_androidapp_dashboard.ui.theme.HumDry
import com.example.aerosense_androidapp_dashboard.ui.theme.HumHigh
import com.example.aerosense_androidapp_dashboard.ui.theme.HumIdeal
import com.example.aerosense_androidapp_dashboard.ui.theme.TempCold
import com.example.aerosense_androidapp_dashboard.ui.theme.TempComfort
import com.example.aerosense_androidapp_dashboard.ui.theme.TempHot
import com.example.aerosense_androidapp_dashboard.ui.theme.TempWarm

enum class MetricType {
    TEMPERATURE,
    HUMIDITY
}

@Composable
fun MetricGaugeCard(
    type: MetricType,
    value: Float?,
    minValue: Float?,
    maxValue: Float?,
    modifier: Modifier = Modifier
) {
    val title = if (type == MetricType.TEMPERATURE) "Température" else "Humidité"
    val unit = if (type == MetricType.TEMPERATURE) "°C" else "%"
    val icon: ImageVector = if (type == MetricType.TEMPERATURE) Icons.Default.Thermostat else Icons.Default.WaterDrop

    // Ranges for progress gauge
    val minGauge = if (type == MetricType.TEMPERATURE) 0f else 0f
    val maxGauge = if (type == MetricType.TEMPERATURE) 50f else 100f

    val rawFraction = if (value != null) {
        ((value - minGauge) / (maxGauge - minGauge)).coerceIn(0f, 1f)
    } else 0f

    val animatedFraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = tween(durationMillis = 800),
        label = "gaugeSweep"
    )

    // Palette per type & value
    val primaryColor = if (type == MetricType.TEMPERATURE) {
        when {
            value == null -> MaterialTheme.colorScheme.primary
            value < 18f -> TempCold
            value < 26f -> TempComfort
            value < 31f -> TempWarm
            else -> TempHot
        }
    } else {
        when {
            value == null -> HumIdeal
            value < 35f -> HumDry
            value < 65f -> HumIdeal
            else -> HumHigh
        }
    }

    val gradientColors = if (type == MetricType.TEMPERATURE) {
        listOf(TempCold, TempComfort, TempWarm, TempHot)
    } else {
        listOf(HumDry, HumIdeal, HumHigh)
    }

    val statusBadgeText = if (type == MetricType.TEMPERATURE) {
        when {
            value == null -> "En attente"
            value < 18f -> "Frais"
            value < 26f -> "Idéal"
            value < 31f -> "Chaud"
            else -> "Très Chaud"
        }
    } else {
        when {
            value == null -> "En attente"
            value < 35f -> "Sec"
            value < 65f -> "Optimal"
            else -> "Humide"
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Icon + Title + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusBadgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gauge Centerpiece
            Box(
                modifier = Modifier
                    .size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                Canvas(modifier = Modifier.size(150.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val startAngle = 135f
                    val totalSweep = 270f

                    // Background Track Arc
                    drawArc(
                        color = trackColor,
                        startAngle = startAngle,
                        sweepAngle = totalSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Sweep Arc
                    if (animatedFraction > 0.001f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = gradientColors
                            ),
                            startAngle = startAngle,
                            sweepAngle = totalSweep * animatedFraction,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Value Text inside Gauge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (value != null) String.format("%.1f", value) else "--.-",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-1).sp
                            )
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = "Capteur DHT11",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Min / Max Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Min : ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = if (minValue != null) "${String.format("%.1f", minValue)}$unit" else "--",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Max : ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = if (maxValue != null) "${String.format("%.1f", maxValue)}$unit" else "--",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}
