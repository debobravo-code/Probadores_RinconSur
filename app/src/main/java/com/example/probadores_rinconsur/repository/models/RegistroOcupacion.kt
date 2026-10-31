package com.example.probadores_rinconsur.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Se guarda cuando termina un uso de un probador (alimenta el Historial).
 * Guarda sedeId y numeroProbador (no FK) para que el historial se conserve aunque se edite un probador.
 */
@Entity(tableName = "registros")
data class RegistroOcupacion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sedeId: Int,
    val numeroProbador: Int,
    val inicioMillis: Long,
    val finMillis: Long,
    val prendaOlvidada: Boolean = false,
    val cantidadPrendas: Int = 0   // prendas que llevó la persona (base de las estadísticas del Administrador)
)