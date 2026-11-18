package com.lajara.campusgps.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = VerdeOscuro,
    onPrimary = Blanco,
    secondary = Turquesa,
    tertiary = Naranja,
    background = Crema,
    surface = Blanco,
    onSurface = VerdeOscuro,
    onSurfaceVariant = TextoSecundario,
    primaryContainer = TurquesaClaro,
    onPrimaryContainer = VerdeOscuro,
    errorContainer = ErrorFondo,
    onErrorContainer = ErrorTexto
)

@Composable
fun CampusGPSTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
