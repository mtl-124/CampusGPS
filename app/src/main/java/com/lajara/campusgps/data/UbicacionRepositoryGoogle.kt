package com.lajara.campusgps.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.lajara.campusgps.model.Posicion
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class UbicacionRepositoryGoogle(private val context: Context) : UbicacionRepository {

    private val cliente = LocationServices.getFusedLocationProviderClient(context)

    override fun playServicesDisponible(): Boolean =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) ==
                ConnectionResult.SUCCESS

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

    @SuppressLint("MissingPermission")
    override fun ubicacionEnVivo(): Flow<Posicion> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(Posicion(location.latitude, location.longitude))
                }
            }
        }

        try {
            cliente.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            close(e)
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            cliente.removeLocationUpdates(callback)
        }
    }
}
