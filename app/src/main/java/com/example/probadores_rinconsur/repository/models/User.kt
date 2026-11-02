package com.example.probadores_rinconsur.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = TipoUsuario::class,
            parentColumns = ["id"],
            childColumns = ["tipoUsuarioId"],
            onDelete = ForeignKey.RESTRICT, // no se puede borrar un tipo en uso
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["tipoUsuarioId"])
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,     // Solo para fines académicos (sin hash)
    val tipoUsuarioId: Int    // FK -> tipo_usuarios.id
)

/** Solo el tipo Administrador ve Usuarios, Tipos de usuario y Sedes, y puede crear/editar probadores. */
fun User.esAdministrador(): Boolean =
    tipoUsuarioId == TipoUsuario.ID_ADMINISTRADOR