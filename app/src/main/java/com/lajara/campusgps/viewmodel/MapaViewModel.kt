package com.lajara.campusgps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajara.campusgps.data.LugaresRepository
import com.lajara.campusgps.data.UbicacionRepository
import com.lajara.campusgps.model.Lugar
import com.lajara.campusgps.model.LugarConDistancia
import com.lajara.campusgps.model.Posicion
import com.lajara.campusgps.model.TipoError
import com.lajara.campusgps.model.UbicacionUi
import com.lajara.campusgps.util.distanciaMetros
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    private val _lugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) }
    )
    val lugares: StateFlow<List<LugarConDistancia>> = _lugares.asStateFlow()

    private val _categoria = MutableStateFlow<String?>(null)
    val categoriaSeleccionada: StateFlow<String?> = _categoria.asStateFlow()

    private val _radio = MutableStateFlow<Int?>(null)
    val radioSeleccionado: StateFlow<Int?> = _radio.asStateFlow()

    private val _siguiendo = MutableStateFlow(false)
    val siguiendo: StateFlow<Boolean> = _siguiendo.asStateFlow()
    private var jobSeguimiento: Job? = null

    val categorias: List<String> = lugaresRepo.obtenerLugares()
        .map { it.categoria }
        .distinct()

    val lugaresVisibles: StateFlow<List<LugarConDistancia>> = combine(_lugares, _categoria) { lista, cat ->
        if (cat == null) lista else lista.filter { it.lugar.categoria == cat }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) }
    )

    val idsEnRadio: StateFlow<Set<Int>> = combine(lugaresVisibles, _radio) { lista, rad ->
        if (rad == null) {
            emptySet()
        } else {
            lista.filter { it.metros != null && it.metros <= rad }.map { it.lugar.id }.toSet()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptySet()
    )

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    val masCercano: StateFlow<Lugar?> = lugaresVisibles
        .map { lista -> lista.firstOrNull { it.metros != null }?.lugar }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun buscar() {
        detenerSeguimiento()
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                "Este dispositivo no tiene Google Play Services",
                TipoError.SIN_PLAY_SERVICES
            )
            return
        }
        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
                .onFailure { _estado.value = errorDe(it) }
        }
    }

    fun iniciarSeguimiento() {
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                "Este dispositivo no tiene Google Play Services",
                TipoError.SIN_PLAY_SERVICES
            )
            return
        }
        detenerSeguimiento()
        _estado.value = UbicacionUi.Buscando
        _siguiendo.value = true
        jobSeguimiento = viewModelScope.launch {
            ubicacionRepo.ubicacionEnVivo()
                .catch { e ->
                    _estado.value = errorDe(e)
                    _siguiendo.value = false
                }
                .collect { pos ->
                    _estado.value = UbicacionUi.Lista(pos)
                    actualizarDistancias(pos)
                }
        }
    }

    fun detenerSeguimiento() {
        jobSeguimiento?.cancel()
        jobSeguimiento = null
        _siguiendo.value = false
    }

    fun alternarSeguimiento() {
        if (_siguiendo.value) {
            detenerSeguimiento()
        } else {
            iniciarSeguimiento()
        }
    }

    fun permisoDenegado() {
        detenerSeguimiento()
        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás",
            TipoError.SIN_PERMISO
        )
    }

    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    fun filtrarPorCategoria(categoria: String?) {
        _categoria.value = categoria
        val visibleActual = lugaresVisibles.value
        if (_seleccionado.value != null && visibleActual.none { it.lugar.id == _seleccionado.value }) {
            _seleccionado.value = null
        }
    }

    fun elegirRadio(metros: Int?) {
        _radio.value = if (_radio.value == metros) null else metros
    }

    private fun actualizarDistancias(pos: Posicion) {
        _lugares.value = lugaresRepo.obtenerLugares()
            .map { LugarConDistancia(it, distanciaMetros(pos, it)) }
            .sortedBy { it.metros }
    }

    private fun errorDe(e: Throwable): UbicacionUi.Error = when (e) {
        is SecurityException -> UbicacionUi.Error(
            "Falta el permiso de ubicación",
            TipoError.SIN_PERMISO
        )
        else -> UbicacionUi.Error(
            "No se pudo obtener la ubicación. Activa el GPS e intenta de nuevo",
            TipoError.GPS_APAGADO
        )
    }
}
