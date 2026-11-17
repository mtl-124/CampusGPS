package com.lajara.campusgps.ui

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.lajara.campusgps.model.LugarConDistancia
import com.lajara.campusgps.model.UbicacionUi
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

@Composable
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current

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

    LaunchedEffect(estado) {
        if (estado is UbicacionUi.Lista) {
            mapa.controller.animateTo(
                GeoPoint(estado.posicion.latitud, estado.posicion.longitud),
                16.0,
                1000L
            )
        }
    }

    AndroidView(
        factory = { mapa },
        modifier = modifier,
        update = { vista ->
            vista.overlays.clear()

            lugares.forEach { item ->
                val lugar = item.lugar
                val marcadorLugar = Marker(vista).apply {
                    position = GeoPoint(lugar.latitud, lugar.longitud)
                    title = lugar.nombre
                    snippet = lugar.categoria
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = icon.mutate().apply { setTint(Color.rgb(30, 136, 229)) }
                }
                vista.overlays.add(marcadorLugar)
            }

            if (estado is UbicacionUi.Lista) {
                val marcadorPropio = Marker(vista).apply {
                    position = GeoPoint(estado.posicion.latitud, estado.posicion.longitud)
                    title = "Estás aquí"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = icon.mutate().apply { setTint(Color.RED) }
                }
                vista.overlays.add(marcadorPropio)
                marcadorPropio.showInfoWindow()
            }

            vista.invalidate()
        },
        onRelease = { it.onDetach() }
    )
}