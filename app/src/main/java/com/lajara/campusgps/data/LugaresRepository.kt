package com.lajara.campusgps.data

import com.lajara.campusgps.model.Lugar

interface LugaresRepository {
    fun obtenerLugares(): List<Lugar>
}