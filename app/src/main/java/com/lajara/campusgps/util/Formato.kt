package com.lajara.campusgps.util

import java.util.Locale

fun formatearDistancia(metros: Float): String =
    if (metros < 1000f) {
        "${metros.toInt()} m"
    } else {
        String.format(Locale.US, "%.1f km", metros / 1000f)
    }

fun formatearCoordenadas(latitud: Double, longitud: Double): String =
    String.format(Locale.US, "Lat %.4f · Lon %.4f", latitud, longitud)
