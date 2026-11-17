package com.lajara.campusgps.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.lajara.campusgps.model.Posicion
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class UbicacionRepositoryGoogle(context: Context) : UbicacionRepository {

    private val cliente = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun obtenerPosicion(): Result<Posicion> {
        return try {
            val ubicacion = cliente
                .getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                )
                .await()

            if (ubicacion != null) {
                Result.success(Posicion(ubicacion.latitude, ubicacion.longitude))
            } else {
                Result.failure(Exception("No se pudo obtener la ubicación"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}