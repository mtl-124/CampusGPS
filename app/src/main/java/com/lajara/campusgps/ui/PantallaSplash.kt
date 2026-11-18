package com.lajara.campusgps.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lajara.campusgps.ui.theme.Crema
import com.lajara.campusgps.ui.theme.TextoSecundario
import com.lajara.campusgps.ui.theme.VerdeOscuro

@Composable
fun PantallaSplash(modifier: Modifier = Modifier) {
    val animacionProgreso = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animacionProgreso.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            )
        )
    }

    val alphaVal = animacionProgreso.value
    val scaleVal = 0.85f + (0.15f * animacionProgreso.value)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Crema)
    ) {
        // Onda superior pegada arriba
        OndaSuperior(
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Contenido central animado
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(alphaVal)
                .scale(scaleVal)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PinNaranja(size = 96.dp)

            Spacer(Modifier.height(16.dp))

            Text(
                text = "CampusGPS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeOscuro
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Encuentra tu lugar en el campus",
                fontSize = 14.sp,
                color = TextoSecundario
            )
        }

        // Onda inferior abajo con padding de barra de navegación
        OndaInferior(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}
