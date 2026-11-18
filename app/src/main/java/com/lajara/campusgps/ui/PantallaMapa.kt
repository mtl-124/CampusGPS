package com.lajara.campusgps.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lajara.campusgps.model.UbicacionUi
import com.lajara.campusgps.ui.theme.BordeSuave
import com.lajara.campusgps.ui.theme.Crema
import com.lajara.campusgps.ui.theme.Naranja
import com.lajara.campusgps.ui.theme.TurquesaClaro
import com.lajara.campusgps.ui.theme.VerdeOscuro
import com.lajara.campusgps.util.formatearDistancia
import com.lajara.campusgps.viewmodel.CampusViewModelFactory
import com.lajara.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    val radioSeleccionado by vm.radioSeleccionado.collectAsState()
    val idsEnRadio by vm.idsEnRadio.collectAsState()
    val siguiendo by vm.siguiendo.collectAsState()
    val categorias = vm.categorias

    var fueronMapa by rememberSaveable { mutableStateOf(false) }
    var centrarToken by rememberSaveable { mutableIntStateOf(0) }
    var accionPendiente by rememberSaveable { mutableStateOf("buscar") }

    // Detener seguimiento cuando la app deja de estar visible (ON_STOP)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                vm.detenerSeguimiento()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
        if (resultado.values.any { it }) {
            if (accionPendiente == "iniciar") {
                vm.iniciarSeguimiento()
            } else {
                vm.buscar()
            }
        } else {
            vm.permisoDenegado()
        }
    }

    val verificarYEjecutar: (String) -> Unit = { accion ->
        accionPendiente = accion
        val fine = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            if (accion == "iniciar") {
                vm.iniciarSeguimiento()
            } else {
                centrarToken++
                vm.buscar()
            }
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
            onObtenerUbicacion = { verificarYEjecutar("buscar") },
            onReintentar = { verificarYEjecutar("buscar") }
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
                        .clipToBounds()
                ) {
                    MapaUbicacion(
                        estado = estado,
                        lugares = lugaresVisibles,
                        seleccionado = seleccionado,
                        masCercano = masCercano,
                        centrarToken = centrarToken,
                        radio = radioSeleccionado,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Controles superiores agrupados en un solo Column (Chips y etiqueta "Siguiendo en vivo")
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .padding(end = 60.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Fila de chips de categoría
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
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

                        // Etiqueta "Siguiendo en vivo" (solo si se está siguiendo)
                        if (siguiendo) {
                            Surface(
                                color = Naranja,
                                shape = CircleShape,
                                shadowElevation = 4.dp
                            ) {
                                Text(
                                    text = "Siguiendo en vivo",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Botones flotantes en la columna derecha del mapa (Seguimiento y Centrar)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Botón de seguimiento Iniciar / Detener
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = if (siguiendo) Naranja else VerdeOscuro,
                                shadowElevation = 6.dp,
                                onClick = {
                                    if (siguiendo) {
                                        vm.detenerSeguimiento()
                                    } else {
                                        verificarYEjecutar("iniciar")
                                    }
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (siguiendo) {
                                        Canvas(modifier = Modifier.size(16.dp)) {
                                            drawRect(color = Color.White)
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Iniciar seguimiento",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            // Botón para centrar en mi ubicación
                            Surface(
                                modifier = Modifier.size(48.dp),
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

                        Spacer(Modifier.height(8.dp))

                        // Selector de Radio (Segmented Button Row)
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            val opciones = listOf(100, 200, 500)
                            opciones.forEachIndexed { index, metros ->
                                val seleccionadoBtn = radioSeleccionado == metros
                                SegmentedButton(
                                    selected = seleccionadoBtn,
                                    onClick = { vm.elegirRadio(metros) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = opciones.size),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = TurquesaClaro,
                                        activeContentColor = VerdeOscuro,
                                        inactiveContainerColor = Color.White,
                                        inactiveContentColor = VerdeOscuro
                                    )
                                ) {
                                    Text(
                                        text = "$metros m",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { verificarYEjecutar("buscar") },
                                enabled = !buscando,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp),
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
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
                                        fontSize = 12.sp,
                                        lineHeight = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2
                                    )
                                } else {
                                    IconoNavegacion(size = 16.dp, color = Color.White)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Obtener mi ubicación",
                                        fontSize = 12.sp,
                                        lineHeight = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { masCercano?.let { vm.seleccionar(it.id) } },
                                enabled = masCercano != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp),
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, VerdeOscuro),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = VerdeOscuro
                                )
                            ) {
                                Text(
                                    text = "Ir al más cercano",
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                        }

                        masCercano?.let { lugar ->
                            val metros = lugaresVisibles.firstOrNull { it.lugar.id == lugar.id }?.metros
                            if (metros != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Más cercano: ${lugar.nombre} (${formatearDistancia(metros)})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VerdeOscuro
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        ListaLugares(
                            lugares = lugaresVisibles,
                            seleccionado = seleccionado,
                            masCercanoId = masCercano?.id,
                            idsEnRadio = idsEnRadio,
                            onClick = { vm.seleccionar(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            }
        }
    }
}
