
package com.example.probadores_rinconsur.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Relación entre un usuario y las sedes
 * que tiene asignadas.
 *
 * Un usuario puede tener varias sedes.
 * Una sede puede tener varios usuarios.
 *
 * El administrador podrá asignar o quitar
 * sedes a los trabajadores.
 */
@Entity(
    tableName = "usuario_sede",

    // Evita asignar dos veces la misma sede
    // al mismo usuario.
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

    // Identificador del trabajador
    val usuarioId: Int,

    // Identificador de la sede asignada
    val sedeId: Int
)
