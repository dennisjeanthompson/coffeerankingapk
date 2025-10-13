package com.example.coffeerankingapk.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.coffeerankingapk.ui.theme.PrimaryBrown
import com.example.coffeerankingapk.ui.theme.Success

@Composable
fun AnalyticsChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    chartType: ChartType = ChartType.LINE
) {
    AppCard(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (chartType) {
                ChartType.LINE -> LineChart(dataPoints = dataPoints)
                ChartType.BAR -> BarChart(dataPoints = dataPoints)
            }
        }
    }
}

@Composable
private fun LineChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .height(200.dp)
            .fillMaxSize()
    ) {
        if (dataPoints.isEmpty()) return@Canvas
        
        val maxValue = dataPoints.maxOrNull() ?: 1f
        val minValue = dataPoints.minOrNull() ?: 0f
        val range = maxValue - minValue
        
        val stepX = size.width / (dataPoints.size - 1).coerceAtLeast(1)
        val path = Path()
        
        dataPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - minValue) / range.coerceAtLeast(0.1f)) * size.height
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            // Draw points
            drawCircle(
                color = Success,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
        
        // Draw line
        drawPath(
            path = path,
            color = PrimaryBrown,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

@Composable
private fun BarChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .height(200.dp)
            .fillMaxSize()
    ) {
        if (dataPoints.isEmpty()) return@Canvas
        
        val maxValue = dataPoints.maxOrNull() ?: 1f
        val barWidth = size.width / dataPoints.size * 0.8f
        val spacing = size.width / dataPoints.size * 0.2f
        
        dataPoints.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * size.height
            val x = index * (barWidth + spacing) + spacing / 2
            
            drawRect(
                color = Success,
                topLeft = Offset(x, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

enum class ChartType {
    LINE, BAR
}