package com.lajara.campusgps.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun AppCampusGPS() {
    var mostrarSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1600)
        mostrarSplash = false
    }

    Crossfade(
        targetState = mostrarSplash,
        animationSpec = tween(400),
        label = "SplashCrossfade"
    ) { splash ->
        if (splash) {
            PantallaSplash()
        } else {
            PantallaMapa()
        }
    }
}
