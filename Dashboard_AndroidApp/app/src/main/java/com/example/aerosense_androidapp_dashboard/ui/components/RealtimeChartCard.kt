package com.example.aerosense_androidapp_dashboard.ui.components

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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aerosense_androidapp_dashboard.data.model.HistoryPoint
import com.example.aerosense_androidapp_dashboard.ui.theme.AeroPrimary
import com.example.aerosense_androidapp_dashboard.ui.theme.AeroSecondary

enum class ChartMetricFilter {
    TEMP,
    HUMIDITY
}

@Composable
fun RealtimeChartCard(
    history: List<HistoryPoint>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(ChartMetricFilter.TEMP) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Graphique",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Évolution Temporelle",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${history.size} points enregistrés",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (history.isNotEmpty()) {
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Effacer l'historique",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == ChartMetricFilter.TEMP,
                    onClick = { selectedFilter = ChartMetricFilter.TEMP },
                    label = { Text("Température (°C)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AeroPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = AeroPrimary
                    )
                )

                FilterChip(
                    selected = selectedFilter == ChartMetricFilter.HUMIDITY,
                    onClick = { selectedFilter = ChartMetricFilter.HUMIDITY },
                    label = { Text("Humidité (%)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AeroSecondary.copy(alpha = 0.2f),
                        selectedLabelColor = AeroSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Canvas
            if (history.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (history.isEmpty()) "En attente des premières données MQTT..."
                        else "Acquisition du second point...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
            } else {
                val lineColor = if (selectedFilter == ChartMetricFilter.TEMP) AeroPrimary else AeroSecondary
                val values = history.map {
                    if (selectedFilter == ChartMetricFilter.TEMP) it.temperature else it.humidity
                }
                val minVal = (values.minOrNull() ?: 0f) - 1f
                val maxVal = (values.maxOrNull() ?: 100f) + 1f
                val range = (maxVal - minVal).coerceAtLeast(1f)

                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                            val width = size.width
                            val height = size.height

                            val stepX = width / (values.size - 1).coerceAtLeast(1)

                            // Horizontal reference lines
                            val gridLines = 3
                            for (i in 0..gridLines) {
                                val y = height * (i.toFloat() / gridLines)
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.15f),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Compute points
                            val points = values.mapIndexed { index, v ->
                                val x = index * stepX
                                val y = height - ((v - minVal) / range) * height
                                Offset(x, y)
                            }

                            // Line Path
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (p in points) {
                                    lineTo(p.x, p.y)
                                }
                            }

                            // Filled gradient area under line
                            val fillPath = Path().apply {
                                addPath(linePath)
                                lineTo(points.last().x, height)
                                lineTo(points.first().x, height)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        lineColor.copy(alpha = 0.35f),
                                        lineColor.copy(alpha = 0.0f)
                                    )
                                )
                            )

                            // Draw stroke
                            drawPath(
                                path = linePath,
                                color = lineColor,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Draw dot at latest point
                            val latestPoint = points.last()
                            drawCircle(
                                color = lineColor,
                                radius = 5.dp.toPx(),
                                center = latestPoint
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = latestPoint
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Chart Axis Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val unit = if (selectedFilter == ChartMetricFilter.TEMP) "°C" else "%"
                        Text(
                            text = "Min: ${String.format("%.1f", minVal + 1f)}$unit",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = "Dernière valeur: ${String.format("%.1f", values.last())}$unit",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = lineColor,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = "Max: ${String.format("%.1f", maxVal - 1f)}$unit",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
