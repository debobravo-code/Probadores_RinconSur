package com.example.probadores_rinconsur.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Sucursal de Rincón Sur. Cada sede guarda su propio tiempo de alerta (en minutos):
 * un probador ocupado por más de ese tiempo se muestra en rojo.
 */
@Entity(
    tableName = "sedes",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class Sede(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val tiempoAlerta: Int = 15
) {
    companion object {
        const val MAX_LARGO = 30
        const val ALERTA_MIN = 5
        const val ALERTA_MAX = 60
    }
}