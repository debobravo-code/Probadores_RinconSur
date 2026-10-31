package com.example.probadores_rinconsur.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Estados oficiales. "Con alerta" NO es un estado: es un OCUPADO que pasó el tiempo de alerta de la sede. */
enum class EstadoProbador(val etiqueta: String) {
    LIBRE("Libre"),
    OCUPADO("Ocupado"),
    PRENDA_OLVIDADA("Prenda olvidada"),
    FUERA_DE_SERVICIO("Fuera de servicio")
}

@Entity(
    tableName = "probadores",
    foreignKeys = [
        ForeignKey(
            entity = Sede::class,
            parentColumns = ["id"],
            childColumns = ["sedeId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sedeId", "numero"], unique = true)]
)
data class Probador(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numero: Int,
    val sedeId: Int,
    val estado: EstadoProbador = EstadoProbador.LIBRE,
    val inicioOcupacion: Long? = null,
    val cantidadPrendas: Int = 0
) {
    companion object {
        const val MAX_PRENDAS = 4
    }
}