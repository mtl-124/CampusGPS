package com.lajara.campusgps.data

import com.lajara.campusgps.model.Posicion
import kotlinx.coroutines.flow.Flow

interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
    fun playServicesDisponible(): Boolean
    fun ubicacionEnVivo(): Flow<Posicion>
}
