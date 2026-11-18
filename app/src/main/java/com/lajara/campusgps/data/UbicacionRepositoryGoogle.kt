package com.lajara.campusgps.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
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
        var ultimaUbicacionAceptada: Location? = null

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(5f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // 1. Filtrar por precisión: solo descarta si informa una precisión peor a 50 m
                    //    (el emulador a veces no informa precisión; no debe bloquear todo)
                    if (location.hasAccuracy() && location.accuracy > 50f) {
                        return
                    }

                    // 2. Filtrar por saltos imposibles (> 10 m/s respecto a la última aceptada)
                    val ultima = ultimaUbicacionAceptada
                    if (ultima != null) {
                        val distanciaMetros = ultima.distanceTo(location)
                        val tiempoTranscurridoSeg = (location.time - ultima.time) / 1000.0

                        if (tiempoTranscurridoSeg > 0.0) {
                            val velocidadCalculada = distanciaMetros / tiempoTranscurridoSeg
                            if (velocidadCalculada > 10.0) {
                                // Salto imposible / ruido, descartar
                                return
                            }
                        } else if (distanciaMetros > 1f) {
                            return
                        }
                    }

                    // Aceptar ubicación
                    ultimaUbicacionAceptada = location
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