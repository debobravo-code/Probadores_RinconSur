package com.example.probadores_rinconsur.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["tipoUsuarioId"])
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,     // Solo se envía al crear/editar; el servidor la guarda con BCrypt (en Room queda vacía)
    val tipoUsuarioId: Int    // id del tipo de usuario
)

/** Solo el tipo Administrador ve Usuarios, Tipos de usuario y Sedes, y puede crear/editar probadores. */
fun User.esAdministrador(): Boolean =
    tipoUsuarioId == TipoUsuario.ID_ADMINISTRADOR