package com.lajara.campusgps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lajara.campusgps.model.UbicacionUi
import com.lajara.campusgps.ui.theme.Crema
import com.lajara.campusgps.ui.theme.VerdeOscuro

@Composable
fun PantallaInicio(
    estado: UbicacionUi,
    onObtenerUbicacion: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buscando = estado is UbicacionUi.Buscando

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Crema)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            BarraCampus()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Ilustracion(
                    modifier = Modifier.size(260.dp, 190.dp)
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onObtenerUbicacion,
                    enabled = !buscando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeOscuro,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF5F7F86),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    if (buscando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Buscando...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        IconoNavegacion(size = 20.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Obtener mi ubicación",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (estado is UbicacionUi.Error) {
                    Spacer(Modifier.height(16.dp))
                    MensajeError(
                        error = estado,
                        onReintentar = onReintentar,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            OndaInferior(
                modifier = Modifier.navigationBarsPadding()
            )
        }
    }
}
