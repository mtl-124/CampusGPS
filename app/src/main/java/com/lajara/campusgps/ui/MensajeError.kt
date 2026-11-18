package com.lajara.campusgps.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lajara.campusgps.model.TipoError
import com.lajara.campusgps.model.UbicacionUi
import com.lajara.campusgps.ui.theme.ErrorBorde
import com.lajara.campusgps.ui.theme.ErrorFondo
import com.lajara.campusgps.ui.theme.ErrorTexto

@Composable
fun MensajeError(
    error: UbicacionUi.Error,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ErrorFondo,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, ErrorBorde)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = ErrorTexto,
                modifier = Modifier.size(22.dp)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = error.mensaje,
                color = ErrorTexto,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            when (error.tipo) {
                TipoError.SIN_PERMISO -> {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", contexto.packageName, null)
                            }
                            contexto.startActivity(intent)
                        },
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, ErrorTexto)
                    ) {
                        Text(
                            text = "Abrir ajustes",
                            color = ErrorTexto,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                TipoError.GPS_APAGADO, TipoError.OTRO -> {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onReintentar,
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, ErrorTexto)
                    ) {
                        Text(
                            text = "Reintentar",
                            color = ErrorTexto,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                TipoError.SIN_PLAY_SERVICES -> {}
            }
        }
    }
}
