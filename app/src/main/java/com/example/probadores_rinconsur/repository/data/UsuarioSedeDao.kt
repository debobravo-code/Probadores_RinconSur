package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.repository.models.UsuarioSede
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioSedeDao {

    // Obtiene las sedes asignadas a un usuario
    @Query(
        """
        SELECT sedes.*
        FROM sedes
        INNER JOIN usuario_sede
            ON sedes.id = usuario_sede.sedeId
        WHERE usuario_sede.usuarioId = :usuarioId
        ORDER BY sedes.id ASC
        """
    )
    fun getSedesPorUsuario(
        usuarioId: Int
    ): Flow<List<Sede>>

    // Obtiene las asignaciones actuales del usuario
    @Query(
        "SELECT * FROM usuario_sede WHERE usuarioId = :usuarioId"
    )
    suspend fun getAsignacionesPorUsuario(
        usuarioId: Int
    ): List<UsuarioSede>

    // Asigna una sede al usuario
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(
        asignacion: UsuarioSede
    )

    // Elimina todas las sedes asignadas a un usuario
    @Query(
        "DELETE FROM usuario_sede WHERE usuarioId = :usuarioId"
    )
    suspend fun deletePorUsuario(
        usuarioId: Int
    )
}