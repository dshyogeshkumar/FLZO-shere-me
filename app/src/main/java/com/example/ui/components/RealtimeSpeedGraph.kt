package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue

@Composable
fun RealtimeSpeedGraph(
    speedHistory: List<Float>,
    currentSpeedText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0D121D), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "THROUGHPUT GRAPH",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = currentSpeedText,
                color = ElectricCyan,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Draw background grid lines
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(0f, h * 0.25f),
                    end = Offset(w, h * 0.25f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(0f, h * 0.75f),
                    end = Offset(w, h * 0.75f),
                    strokeWidth = 1f
                )

                if (speedHistory.size >= 2) {
                    val maxVal = (speedHistory.maxOrNull() ?: 100f).coerceAtLeast(10f)
                    val stepX = w / (speedHistory.size - 1)

                    val path = Path()
                    speedHistory.forEachIndexed { idx, speed ->
                        val x = idx * stepX
                        val norm = (speed / maxVal).coerceIn(0f, 1f)
                        val y = h - (norm * (h * 0.85f))
                        if (idx == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = ElectricCyan,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
