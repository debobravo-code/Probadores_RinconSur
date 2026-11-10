
package com.example.probadores_rinconsur.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Se guarda cuando termina un uso de un probador.
 * Alimenta el Historial de ocupación.
 *
 * Guarda sedeId y numeroProbador para conservar
 * el historial aunque se edite un probador.
 */
@Entity(tableName = "registros")
data class RegistroOcupacion(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val sedeId: Int,

    val numeroProbador: Int,

    val inicioMillis: Long,

    val finMillis: Long,

    // Indica si hubo una prenda olvidada
    val prendaOlvidada: Boolean = false,

    // Cantidad de prendas que llevó el cliente
    val cantidadPrendas: Int = 0,

    // Observación opcional del registro
    val observacion: String = ""
)
