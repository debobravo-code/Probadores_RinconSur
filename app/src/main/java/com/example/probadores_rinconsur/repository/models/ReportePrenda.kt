package com.example.probadores_rinconsur.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Reporte de prenda olvidada (sin datos personales de clientes). La foto se toma con CameraX. */
@Entity(
    tableName = "reportes_prenda",
    foreignKeys = [
        ForeignKey(
            entity = Probador::class,
            parentColumns = ["id"],
            childColumns = ["probadorId"],
            onDelete = ForeignKey.RESTRICT, // no se puede borrar un probador con reportes
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["probadorId"])]
)
data class ReportePrenda(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val probadorId: Int,                 // FK -> probadores.id
    val descripcion: String,
    val fotoUri: String? = null,         // Foto de la prenda tomada con CameraX
    val fechaMillis: Long
)