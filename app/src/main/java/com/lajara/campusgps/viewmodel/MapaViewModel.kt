
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

    // Estado general de la ubicación
    private val _estado =
        MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)

    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    // Lista de lugares
    private val _lugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map {
            LugarConDistancia(it, null)
        }
    )

    val lugares: StateFlow<List<LugarConDistancia>> =
        _lugares.asStateFlow()

    // Filtro por categoría
    private val _categoria = MutableStateFlow<String?>(null)

    val categoriaSeleccionada: StateFlow<String?> =
        _categoria.asStateFlow()

    // Radio de búsqueda
    private val _radio = MutableStateFlow<Int?>(null)

    val radioSeleccionado: StateFlow<Int?> =
        _radio.asStateFlow()

    // Seguimiento en vivo
    private val _siguiendo = MutableStateFlow(false)

    val siguiendo: StateFlow<Boolean> =
        _siguiendo.asStateFlow()

    private var jobSeguimiento: Job? = null

    // Categorías disponibles
    val categorias: List<String> =
        lugaresRepo.obtenerLugares()
            .map { it.categoria }
            .distinct()

    // Lugares visibles según la categoría seleccionada
    private val _lugaresVisibles: StateFlow<List<LugarConDistancia>> =
        combine(_lugares, _categoria) { lista, categoria ->
            if (categoria == null) {
                lista
            } else {
                lista.filter {
                    it.lugar.categoria == categoria
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = lugaresRepo.obtenerLugares().map {
                LugarConDistancia(it, null)
            }
        )

    val lugaresVisibles: StateFlow<List<LugarConDistancia>> =
        _lugaresVisibles

    // Lugares dentro del radio seleccionado
    val idsEnRadio: StateFlow<Set<Int>> =
        combine(_lugaresVisibles, _radio) { lista, radio ->
            if (radio == null) {
                emptySet()
            } else {
                lista.filter {
                    it.metros != null && it.metros <= radio
                }.map {
                    it.lugar.id
                }.toSet()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    // Lugar seleccionado
    private val _seleccionado = MutableStateFlow<Int?>(null)

    val seleccionado: StateFlow<Int?> =
        _seleccionado.asStateFlow()

    // Lugar más cercano
    val masCercano: StateFlow<Lugar?> =
        _lugaresVisibles
            .map { lista ->
                lista.firstOrNull {
                    it.metros != null
                }?.lugar
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    // Buscar ubicación actual
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
                .onSuccess { posicion ->
                    _estado.value = UbicacionUi.Lista(posicion)
                    actualizarDistancias(posicion)
                }
                .onFailure { error ->
                    _estado.value = errorDe(error)
                }
        }
    }

    // Iniciar seguimiento en vivo
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
                .catch { error ->
                    _estado.value = errorDe(error)
                    _siguiendo.value = false
                }
                .collect { posicion ->
                    _estado.value = UbicacionUi.Lista(posicion)
                    actualizarDistancias(posicion)
                }
        }
    }

    // Detener seguimiento
    fun detenerSeguimiento() {
        jobSeguimiento?.cancel()
        jobSeguimiento = null
        _siguiendo.value = false
    }

    // Alternar seguimiento
    fun alternarSeguimiento() {
        if (_siguiendo.value) {
            detenerSeguimiento()
        } else {
            iniciarSeguimiento()
        }
    }

    // Cuando se deniega el permiso
    fun permisoDenegado() {
        detenerSeguimiento()

        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás",
            TipoError.SIN_PERMISO
        )
    }

    // Seleccionar un lugar
    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    // Filtrar por categoría
    fun filtrarPorCategoria(categoria: String?) {
        _categoria.value = categoria

        val visibleActual = _lugaresVisibles.value

        if (
            _seleccionado.value != null &&
            visibleActual.none {
                it.lugar.id == _seleccionado.value
            }
        ) {
            _seleccionado.value = null
        }
    }

    // Elegir radio de búsqueda
    fun elegirRadio(metros: Int?) {
        _radio.value =
            if (_radio.value == metros) null else metros
    }

    // Actualizar distancias de los lugares
    private fun actualizarDistancias(posicion: Posicion) {
        _lugares.value = lugaresRepo.obtenerLugares()
            .map { lugar ->
                LugarConDistancia(
                    lugar,
                    distanciaMetros(posicion, lugar)
                )
            }
            .sortedBy { it.metros }
    }

    // Manejar errores
    private fun errorDe(error: Throwable): UbicacionUi.Error {
        return when (error) {
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
}
