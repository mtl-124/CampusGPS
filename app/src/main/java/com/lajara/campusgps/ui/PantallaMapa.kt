package com.lajara.campusgps.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lajara.campusgps.model.UbicacionUi
import com.lajara.campusgps.ui.theme.BordeSuave
import com.lajara.campusgps.ui.theme.Crema
import com.lajara.campusgps.ui.theme.VerdeOscuro
import com.lajara.campusgps.util.formatearDistancia
import com.lajara.campusgps.viewmodel.CampusViewModelFactory
import com.lajara.campusgps.viewmodel.MapaViewModel

@Composable
fun PantallaMapa(
    vm: MapaViewModel = viewModel(factory = CampusViewModelFactory(LocalContext.current))
) {
    val contexto = LocalContext.current
    val estado by vm.estado.collectAsState()
    val lugaresVisibles by vm.lugaresVisibles.collectAsState()
    val seleccionado by vm.seleccionado.collectAsState()
    val masCercano by vm.masCercano.collectAsState()
    val categoriaSeleccionada by vm.categoriaSeleccionada.collectAsState()
    val categorias = vm.categorias

    var fueronMapa by rememberSaveable { mutableStateOf(false) }
    var centrarToken by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(estado) {
        when (estado) {
            is UbicacionUi.Lista -> {
                fueronMapa = true
            }
            is UbicacionUi.Inicial, is UbicacionUi.Error -> {
                fueronMapa = false
            }
            is UbicacionUi.Buscando -> {
                // No cambia durante Buscando para evitar parpadeos
            }
        }
    }

    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) vm.buscar() else vm.permisoDenegado()
    }

    val alPulsar: () -> Unit = {
        val fine = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            centrarToken++
            vm.buscar()
        } else {
            lanzador.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    if (!fueronMapa) {
        PantallaInicio(
            estado = estado,
            onObtenerUbicacion = alPulsar,
            onReintentar = alPulsar
        )
    } else {
        val buscando = estado is UbicacionUi.Buscando

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Crema)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                BarraCampus()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    MapaUbicacion(
                        estado = estado,
                        lugares = lugaresVisibles,
                        seleccionado = seleccionado,
                        masCercano = masCercano,
                        centrarToken = centrarToken,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Fila de chips de categoría arriba a la izquierda
                    LazyRow(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val seleccionada = categoriaSeleccionada == null
                            FilterChip(
                                selected = seleccionada,
                                onClick = { vm.filtrarPorCategoria(null) },
                                label = { Text("Todas") },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White.copy(alpha = 0.95f),
                                    labelColor = VerdeOscuro,
                                    selectedContainerColor = VerdeOscuro,
                                    selectedLabelColor = Color.White
                                ),
                                border = if (seleccionada) null else FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false,
                                    borderColor = BordeSuave
                                ),
                                elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp)
                            )
                        }
                        items(categorias) { cat ->
                            val seleccionada = categoriaSeleccionada == cat
                            FilterChip(
                                selected = seleccionada,
                                onClick = { vm.filtrarPorCategoria(cat) },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White.copy(alpha = 0.95f),
                                    labelColor = VerdeOscuro,
                                    selectedContainerColor = VerdeOscuro,
                                    selectedLabelColor = Color.White
                                ),
                                border = if (seleccionada) null else FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false,
                                    borderColor = BordeSuave
                                ),
                                elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp)
                            )
                        }
                    }

                    // Botón flotante para centrar en mi ubicación (abajo a la derecha)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(48.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 6.dp,
                        onClick = { centrarToken++ }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            IconoMira(size = 24.dp, color = VerdeOscuro)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    shadowElevation = 12.dp,
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Barra de arrastre decorativa
                        Box(
                            modifier = Modifier
                                .size(40.dp, 4.dp)
                                .background(Color(0xFFC9D6D8), CircleShape)
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = alPulsar,
                                enabled = !buscando,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerdeOscuro,
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF5F7F86),
                                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                                )
                            ) {
                                if (buscando) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Buscando...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                } else {
                                    IconoNavegacion(size = 16.dp, color = Color.White)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Obtener mi ubicación",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { masCercano?.let { vm.seleccionar(it.id) } },
                                enabled = masCercano != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = CircleShape,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, VerdeOscuro),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = VerdeOscuro
                                )
                            ) {
                                Text(
                                    text = "Ir al más cercano",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }

                        masCercano?.let { lugar ->
                            val metros = lugaresVisibles.firstOrNull { it.lugar.id == lugar.id }?.metros
                            if (metros != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Más cercano: ${lugar.nombre} (${formatearDistancia(metros)})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VerdeOscuro
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        ListaLugares(
                            lugares = lugaresVisibles,
                            seleccionado = seleccionado,
                            masCercanoId = masCercano?.id,
                            onClick = { vm.seleccionar(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }
        }
    }
}
