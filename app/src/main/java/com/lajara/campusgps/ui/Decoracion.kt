package com.lajara.campusgps.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.lajara.campusgps.ui.theme.Naranja
import com.lajara.campusgps.ui.theme.Turquesa
import com.lajara.campusgps.ui.theme.VerdeOscuro

@Composable
fun OndaSuperior(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val w = size.width
        val h = size.height

        // 1. Banda NARANJA (abajo)
        val pathNaranja = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, h * 0.7f)
            cubicTo(w * 0.3f, h * 1.1f, w * 0.7f, h * 0.5f, w, h * 0.85f)
            lineTo(w, 0f)
            close()
        }
        drawPath(pathNaranja, color = Naranja)

        // 2. Banda TURQUESA (medio)
        val pathTurquesa = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, h * 0.45f)
            cubicTo(w * 0.25f, h * 0.85f, w * 0.6f, h * 0.2f, w, h * 0.6f)
            lineTo(w, 0f)
            close()
        }
        drawPath(pathTurquesa, color = Turquesa)

        // 3. Banda VERDEOSCURO (arriba)
        val pathVerde = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, h * 0.2f)
            cubicTo(w * 0.35f, h * 0.6f, w * 0.65f, h * 0.1f, w, h * 0.35f)
            lineTo(w, 0f)
            close()
        }
        drawPath(pathVerde, color = VerdeOscuro)
    }
}

@Composable
fun OndaInferior(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        val w = size.width
        val h = size.height

        val pathVerde = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.65f)
            cubicTo(w * 0.35f, h * 0.4f, w * 0.65f, h * 0.9f, w, h * 0.65f)
            lineTo(w, h)
            close()
        }
        drawPath(pathVerde, color = VerdeOscuro)

        val pathTurquesa = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.55f)
            cubicTo(w * 0.25f, h * 0.15f, w * 0.6f, h * 0.8f, w, h * 0.4f)
            lineTo(w, h)
            close()
        }
        drawPath(pathTurquesa, color = Turquesa.copy(alpha = 0.7f))
    }
}

@Composable
fun IconoNavegacion(size: Dp, color: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h)
            lineTo(w * 0.5f, h * 0.75f)
            lineTo(0f, h)
            close()
        }
        drawPath(path, color = color)
    }
}

@Composable
fun IconoMira(size: Dp, color: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = w * 0.35f

        // Círculo exterior
        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))
        // Punto central
        drawCircle(color = color, radius = w * 0.12f, center = center)

        // Marcas
        val tickLen = w * 0.15f
        val strokeW = 2.dp.toPx()
        // Arriba
        drawLine(color, Offset(center.x, center.y - radius - tickLen), Offset(center.x, center.y - radius), strokeW)
        // Abajo
        drawLine(color, Offset(center.x, center.y + radius), Offset(center.x, center.y + radius + tickLen), strokeW)
        // Izquierda
        drawLine(color, Offset(center.x - radius - tickLen, center.y), Offset(center.x - radius, center.y), strokeW)
        // Derecha
        drawLine(color, Offset(center.x + radius, center.y), Offset(center.x + radius + tickLen, center.y), strokeW)
    }
}

@Composable
fun PinNaranja(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h * 0.38f
        val r = w * 0.38f

        val path = Path().apply {
            moveTo(cx, h)
            lineTo(cx - r * 0.8f, cy + r * 0.5f)
            arcTo(Rect(cx - r, cy - r, cx + r, cy + r), 45f, 90f, false)
            arcTo(Rect(cx - r, cy - r, cx + r, cy + r), 135f, 225f, false)
            close()
        }
        drawPath(path, color = Naranja)
        drawCircle(color = Color.White, radius = r * 0.4f, center = Offset(cx, cy))
    }
}

@Composable
fun Ilustracion(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Suelo
        drawOval(
            color = Color(0xFFDCEEEC),
            topLeft = Offset(w * 0.05f, h * 0.70f),
            size = Size(w * 0.90f, h * 0.25f)
        )

        // 2. Nubes
        val nubeColor = Color(0xFFCFE8E8)
        // Nube izquierda
        drawCircle(nubeColor, radius = w * 0.06f, center = Offset(w * 0.18f, h * 0.42f))
        drawCircle(nubeColor, radius = w * 0.08f, center = Offset(w * 0.23f, h * 0.38f))
        drawCircle(nubeColor, radius = w * 0.05f, center = Offset(w * 0.28f, h * 0.43f))

        // Nube derecha
        drawCircle(nubeColor, radius = w * 0.07f, center = Offset(w * 0.82f, h * 0.24f))
        drawCircle(nubeColor, radius = w * 0.05f, center = Offset(w * 0.77f, h * 0.27f))

        // 3. Edificio central
        val edifColor = Color(0xFF8FB3B8)
        val torreColor = Color(0xFF7FA6AC)
        val winColor = Color(0xFFE3F1F2)

        // Cuerpo principal
        drawRect(edifColor, topLeft = Offset(w * 0.28f, h * 0.45f), size = Size(w * 0.44f, h * 0.35f))
        // Torre central
        drawRect(torreColor, topLeft = Offset(w * 0.45f, h * 0.30f), size = Size(w * 0.14f, h * 0.50f))

        // Reloj en la torre
        drawCircle(Color.White, radius = w * 0.035f, center = Offset(w * 0.52f, h * 0.38f))
        drawCircle(VerdeOscuro, radius = w * 0.035f, center = Offset(w * 0.52f, h * 0.38f), style = Stroke(2f))
        drawLine(VerdeOscuro, Offset(w * 0.52f, h * 0.38f), Offset(w * 0.52f, h * 0.36f), 2f)
        drawLine(VerdeOscuro, Offset(w * 0.52f, h * 0.38f), Offset(w * 0.54f, h * 0.38f), 2f)

        // Puerta
        drawRect(VerdeOscuro, topLeft = Offset(w * 0.50f, h * 0.70f), size = Size(w * 0.04f, h * 0.10f))

        // Ventanas izq y der
        val winW = w * 0.04f
        val winH = w * 0.06f
        for (row in 0..1) {
            for (col in 0..2) {
                val startXIzq = w * 0.31f + col * (winW + w * 0.02f)
                val startY = h * 0.50f + row * (winH + h * 0.04f)
                drawRect(winColor, topLeft = Offset(startXIzq, startY), size = Size(winW, winH))

                val startXDer = w * 0.60f + col * (winW + w * 0.02f)
                drawRect(winColor, topLeft = Offset(startXDer, startY), size = Size(winW, winH))
            }
        }

        // 4. Árboles
        // Árbol izq
        drawRect(VerdeOscuro, topLeft = Offset(w * 0.20f, h * 0.62f), size = Size(w * 0.03f, h * 0.15f))
        drawOval(Turquesa, topLeft = Offset(w * 0.14f, h * 0.52f), size = Size(w * 0.15f, h * 0.15f))
        drawOval(Turquesa, topLeft = Offset(w * 0.16f, h * 0.46f), size = Size(w * 0.11f, h * 0.12f))

        // Árbol der
        drawRect(VerdeOscuro, topLeft = Offset(w * 0.78f, h * 0.60f), size = Size(w * 0.03f, h * 0.15f))
        drawOval(Turquesa, topLeft = Offset(w * 0.73f, h * 0.50f), size = Size(w * 0.14f, h * 0.14f))
        drawOval(Turquesa, topLeft = Offset(w * 0.75f, h * 0.45f), size = Size(w * 0.10f, h * 0.11f))

        // 5. Camino punteado
        val pathCamino = Path().apply {
            moveTo(w * 0.12f, h * 0.76f)
            cubicTo(w * 0.35f, h * 0.95f, w * 0.65f, h * 0.95f, w * 0.88f, h * 0.78f)
        }
        drawPath(
            path = pathCamino,
            color = Turquesa,
            style = Stroke(
                width = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f))
            )
        )

        // 6. Pin grande naranja flotando
        val pinCx = w * 0.45f
        val pinCy = h * 0.18f
        val pinR = w * 0.10f
        val pinPath = Path().apply {
            moveTo(pinCx, pinCy + pinR * 1.8f)
            lineTo(pinCx - pinR * 0.8f, pinCy)
            arcTo(Rect(pinCx - pinR, pinCy - pinR, pinCx + pinR, pinCy + pinR), 180f, 180f, false)
            close()
        }
        drawPath(pinPath, color = Naranja)
        drawCircle(Color.White, radius = pinR * 0.35f, center = Offset(pinCx, pinCy))

        // Rayitos alrededor del pin
        val rayColor = Naranja
        drawLine(rayColor, Offset(pinCx - pinR * 1.4f, pinCy - pinR * 0.8f), Offset(pinCx - pinR * 1.0f, pinCy - pinR * 0.5f), 2.dp.toPx())
        drawLine(rayColor, Offset(pinCx - pinR * 1.6f, pinCy - pinR * 0.2f), Offset(pinCx - pinR * 1.1f, pinCy - pinR * 0.2f), 2.dp.toPx())
        drawLine(rayColor, Offset(pinCx - pinR * 1.3f, pinCy + pinR * 0.3f), Offset(pinCx - pinR * 0.9f, pinCy + pinR * 0.1f), 2.dp.toPx())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraCampus() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VerdeOscuro)
            .zIndex(1f)
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "CampusGPS",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = VerdeOscuro,
                titleContentColor = Color.White
            )
        )
        OndaSuperior()
    }
}
