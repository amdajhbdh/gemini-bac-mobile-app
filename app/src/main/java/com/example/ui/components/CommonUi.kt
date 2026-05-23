package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        action?.invoke()
    }
}

@Composable
fun CustomChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else color.copy(alpha = 0.4f),
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        modifier = modifier.border(
            width = 1.dp,
            color = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

// ============================================
// WHITEBOARD VECTOR CANVAS INDENTATION DRAWINGS
// ============================================

@Composable
fun MathWhiteboardDrawing(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF64748B), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("BOARD 01: LIMITS & EQUATIONS", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("dx/dy", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                val width = size.width
                val height = size.height
                val midY = height / 2

                // Draw coordinate axes
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(0f, midY),
                    end = Offset(width, midY),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(width / 4, 0f),
                    end = Offset(width / 4, height),
                    strokeWidth = 2f
                )

                // Draw curve: f(x) = sin(x)/x style curve with a hole at x=0 (which is width/4)
                val curvePath = Path()
                var first = true
                for (px in 0..width.toInt()) {
                    val xVal = (px - width / 4) / 40f
                    if (xVal == 0f) continue
                    val yVal = Math.sin(xVal.toDouble()) / xVal
                    val py = midY - (yVal * 150).toFloat()

                    if (first) {
                        curvePath.moveTo(px.toFloat(), py)
                        first = false
                    } else {
                        curvePath.lineTo(px.toFloat(), py)
                    }
                }
                drawPath(
                    path = curvePath,
                    color = Color(0xFF38BDF8),
                    style = Stroke(width = 4f)
                )

                // Draw limit convergence lines approaching x=0
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 8f,
                    center = Offset(width / 4, midY - 150f),
                    style = Stroke(width = 3f)
                )

                // Text representation coordinates
                drawLine(
                    color = Color(0xFFF1F5F9),
                    start = Offset(width / 4 - 30f, midY - 150f),
                    end = Offset(width / 4 + 30f, midY - 150f),
                    strokeWidth = 1f
                )
            }
            Text("Lim [x->0] sin(x)/x = 1.0", color = Color(0xFFF1F5F9), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun PhysicsWhiteboardDrawing(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("BOARD 02: 2D VECTOR KINEMATICS", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("ay = -g", color = Color(0xFFF43F5E), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                val width = size.width
                val height = size.height

                // Ground line
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(0f, height - 20f),
                    end = Offset(width, height - 20f),
                    strokeWidth = 3f
                )

                // Projectile motion curve (parabola)
                val parabola = Path()
                parabola.moveTo(20f, height - 20f)
                // Quadratic bezier to form parabola
                parabola.quadraticTo(width / 2, -50f, width - 20f, height - 20f)
                drawPath(
                    path = parabola,
                    color = Color(0xFF10B981),
                    style = Stroke(width = 4f)
                )

                // Launch vector arrow
                drawLine(
                    color = Color(0xFFF43F5E),
                    start = Offset(20f, height - 20f),
                    end = Offset(130f, height - 120f),
                    strokeWidth = 4f
                )
                // Arrow tip
                drawLine(color = Color(0xFFF43F5E), start = Offset(130f, height - 120f), end = Offset(100f, height - 120f), strokeWidth = 4f)
                drawLine(color = Color(0xFFF43F5E), start = Offset(130f, height - 120f), end = Offset(130f, height - 90f), strokeWidth = 4f)

                // Gravity vector pointing down at peak
                drawLine(
                    color = Color(0xFFF59E0B),
                    start = Offset(width / 2, height / 3),
                    end = Offset(width / 2, height / 3 + 60f),
                    strokeWidth = 3f
                )
            }
            Text("ay = -9.8m/s² | ax = 0 | Vx = constant", color = Color(0xFFF1F5F9), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun BiologyWhiteboardDrawing(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF1F2937), RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF4B5563), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("BOARD 03: MITOCHONDRION CELLULAR CYCLE", color = Color(0xFF9CA3AF), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("ATP+CO2", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                val width = size.width
                val height = size.height
                val cx = width / 2
                val cy = height / 2
                val r = Math.min(width, height) / 3

                // Krebs cycle circle
                drawCircle(
                    color = Color(0xFF818CF8),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 4f)
                )

                // Draw connection nodes representing Acetyl-CoA -> Citrate etc.
                drawCircle(color = Color(0xFF10B981), radius = 10f, center = Offset(cx, cy - r)) // TOP: Citrate
                drawCircle(color = Color(0xFFF59E0B), radius = 10f, center = Offset(cx + r, cy)) // RIGHT: Succinate
                drawCircle(color = Color(0xFF38BDF8), radius = 10f, center = Offset(cx, cy + r)) // BOTTOM: Malate
                drawCircle(color = Color(0xFFEC4899), radius = 10f, center = Offset(cx - r, cy)) // LEFT: Oxaloacetate

                // Input line from top left
                drawLine(
                    color = Color(0xFFF1F5F9),
                    start = Offset(20f, 20f),
                    end = Offset(cx, cy - r),
                    strokeWidth = 3f
                )
            }
            Text("Acetyl-CoA -> Krebs Cycle -> NADH + CO₂ + ATP", color = Color(0xFFF1F5F9), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
