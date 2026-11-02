package com.example.probadores_rinconsur.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Relación entre un usuario y las sedes que tiene asignadas.
 */
@Entity(
    tableName = "usuario_sede",
    primaryKeys = ["usuarioId", "sedeId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Sede::class,
            parentColumns = ["id"],
            childColumns = ["sedeId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["sedeId"])
    ]
)
data class UsuarioSede(
    val usuarioId: Int,
    val sedeId: Int
)