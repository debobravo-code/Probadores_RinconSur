package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.repository.models.UsuarioSede
import kotlinx.coroutines.flow.Flow

/**
 * Sedes asignadas a cada usuario (copia local de MySQL).
 *
 * El administrador asigna o quita sedes.
 * El personal solo accede a las sedes que tiene asignadas.
 */
@Dao
abstract class UsuarioSedeDao {

    // Solo las sedes asignadas al usuario indicado
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
    abstract fun getSedesPorUsuario(usuarioId: Int): Flow<List<Sede>>

    // IGNORE evita duplicar una asignación que ya existe
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(asignacion: UsuarioSede)

    @Query("DELETE FROM usuario_sede WHERE usuarioId = :usuarioId")
    abstract suspend fun deletePorUsuario(usuarioId: Int)

    // Cambia todas las sedes de un usuario en una sola transacción
    @Transaction
    open suspend fun reemplazarSedes(usuarioId: Int, sedesIds: List<Int>) {
        deletePorUsuario(usuarioId)
        sedesIds.forEach { insert(UsuarioSede(usuarioId, it)) }
    }
}
