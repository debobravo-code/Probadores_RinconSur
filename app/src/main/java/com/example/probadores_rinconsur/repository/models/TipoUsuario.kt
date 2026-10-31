package com.example.probadores_rinconsur.repository.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tipo de usuario: Administrador, Personal, etc.
 * tipo_usuario: máximo 30 caracteres (SQLite no limita largo; se valida en ViewModel y vista).
 */
@Entity(
    tableName = "tipo_usuarios",
    indices = [Index(value = ["tipo_usuario"], unique = true)]
)
data class TipoUsuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "tipo_usuario") val tipoUsuario: String
) {
    companion object {
        const val MAX_LARGO = 30
        const val ID_ADMINISTRADOR = 1 // el tipo 1 (Administrador) se crea al instalar la app
    }
}