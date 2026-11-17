package com.lajara.campusgps.data

import com.lajara.campusgps.model.Lugar

class LugaresRepositoryLocal : LugaresRepository {

    override fun obtenerLugares(): List<Lugar> = listOf(
        Lugar(1, "Biblioteca Central", "Estudio", -12.044955, -76.952893),
        Lugar(2, "Cafetería Campus", "Comida", -12.044942, -76.952373),
        Lugar(3, "Laboratorio de Cómputo", "Estudio", -12.044444, -76.952963),
        Lugar(4, "Auditorio Principal", "Eventos", -12.044719, -76.952767),
        Lugar(5, "Estacionamiento Norte", "Servicios", -12.044410, -76.951718)
    )
}