package com.lajara.campusgps.ui

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.lajara.campusgps.model.Lugar
import com.lajara.campusgps.model.LugarConDistancia
import com.lajara.campusgps.model.UbicacionUi
import com.lajara.campusgps.util.formatearCoordenadas
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.io.File

@Composable
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    masCercano: Lugar?,
    centrarToken: Int,
    radio: Int?,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val estadoActual by rememberUpdatedState(estado)

    val prevSeleccionado = remember { mutableStateOf<Int?>(null) }
    val wasLista = remember { mutableStateOf(false) }

    val mapa = remember {
        Configuration.getInstance().apply {
            load(contexto, contexto.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = contexto.packageName
            osmdroidBasePath = File(contexto.cacheDir, "osmdroid")
            osmdroidTileCache = File(contexto.cacheDir, "osmdroid/tiles")
        }
        MapView(contexto).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(12.0)
            controller.setCenter(GeoPoint(-12.0464, -77.0428))
        }
    }

    DisposableEffect(mapa) {
        mapa.onResume()
        onDispose { mapa.onPause() }
    }

    LaunchedEffect(centrarToken, estado is UbicacionUi.Lista) {
        val est = estadoActual
        if (est is UbicacionUi.Lista) {
            mapa.controller.animateTo(
                GeoPoint(est.posicion.latitud, est.posicion.longitud),
                16.0,
                1000L
            )
        }
    }

    LaunchedEffect(seleccionado) {
        val lugar = lugares.firstOrNull { it.lugar.id == seleccionado }?.lugar
        if (lugar != null) {
            mapa.controller.animateTo(
                GeoPoint(lugar.latitud, lugar.longitud),
                17.0,
                1000L
            )
        }
    }

    AndroidView(
        factory = { mapa },
        modifier = modifier,
        update = { vista ->
            vista.overlays.clear()

            // 1. Dibujar círculo de radio si aplica y hay ubicación (antes que los marcadores)
            val rad = radio
            val est = estadoActual
            if (est is UbicacionUi.Lista && rad != null) {
                val circulo = Polygon().apply {
                    val puntos = Polygon.pointsAsCircle(GeoPoint(est.posicion.latitud, est.posicion.longitud), rad.toDouble())
                    setPoints(puntos)
                    setFillColor(Color.argb(40, 31, 162, 166)) // Turquesa con alfa ~40/255
                    setStrokeColor(Color.rgb(31, 162, 166)) // Turquesa
                    setStrokeWidth(4f)
                }
                vista.overlays.add(circulo)
            }

            var marcadorElegido: Marker? = null

            lugares.forEach { item ->
                val lugar = item.lugar
                val marcadorLugar = Marker(vista).apply {
                    position = GeoPoint(lugar.latitud, lugar.longitud)
                    title = lugar.nombre
                    snippet = lugar.categoria
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = icon.mutate().apply {
                        setTint(
                            if (lugar.id == masCercano?.id) Color.rgb(52, 168, 83) else Color.rgb(30, 136, 229)
                        )
                    }
                }
                vista.overlays.add(marcadorLugar)
                if (lugar.id == seleccionado) marcadorElegido = marcadorLugar
            }

            var marcadorPropio: Marker? = null
            if (est is UbicacionUi.Lista) {
                marcadorPropio = Marker(vista).apply {
                    position = GeoPoint(est.posicion.latitud, est.posicion.longitud)
                    title = "Estás aquí"
                    snippet = formatearCoordenadas(est.posicion.latitud, est.posicion.longitud)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = icon.mutate().apply { setTint(Color.RED) }
                }
                vista.overlays.add(marcadorPropio)
            }

            // Solo mostrar ventana de información si cambió la selección o es la primera vez que entra a Lista
            val seleccionCambio = seleccionado != prevSeleccionado.value
            val ingresoListaPorPrimeraVez = est is UbicacionUi.Lista && !wasLista.value

            if (seleccionCambio || ingresoListaPorPrimeraVez) {
                if (marcadorElegido != null) {
                    marcadorElegido?.showInfoWindow()
                } else if (ingresoListaPorPrimeraVez) {
                    marcadorPropio?.showInfoWindow()
                }
                prevSeleccionado.value = seleccionado
            }
            wasLista.value = est is UbicacionUi.Lista

            vista.invalidate()
        },
        onRelease = { it.onDetach() }
    )
}
