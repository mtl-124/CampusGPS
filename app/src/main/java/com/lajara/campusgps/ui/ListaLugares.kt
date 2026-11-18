package com.lajara.campusgps.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lajara.campusgps.model.LugarConDistancia
import com.lajara.campusgps.ui.theme.AzulLugar
import com.lajara.campusgps.ui.theme.BordeSuave
import com.lajara.campusgps.ui.theme.Turquesa
import com.lajara.campusgps.ui.theme.TurquesaClaro
import com.lajara.campusgps.ui.theme.TextoSecundario
import com.lajara.campusgps.ui.theme.VerdeCercano
import com.lajara.campusgps.ui.theme.VerdeOscuro
import com.lajara.campusgps.util.formatearDistancia

@Composable
fun ListaLugares(
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    masCercanoId: Int?,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(lugares, key = { it.lugar.id }) { item ->
            val elegido = item.lugar.id == seleccionado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(item.lugar.id) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (elegido) {
                        TurquesaClaro
                    } else {
                        androidx.compose.ui.graphics.Color.White
                    }
                ),
                border = if (elegido) {
                    BorderStroke(2.dp, Turquesa)
                } else {
                    BorderStroke(1.dp, BordeSuave)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (item.lugar.id == masCercanoId) VerdeCercano else AzulLugar,
                                CircleShape
                            )
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.lugar.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = VerdeOscuro
                        )
                        Text(
                            text = item.lugar.categoria,
                            fontSize = 12.sp,
                            color = TextoSecundario
                        )
                    }
                    Text(
                        text = item.metros?.let { formatearDistancia(it) } ?: "Sin calcular",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = VerdeOscuro
                    )
                }
            }
        }
    }
}
