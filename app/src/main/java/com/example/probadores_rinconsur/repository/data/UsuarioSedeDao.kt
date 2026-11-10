
package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.repository.models.UsuarioSede
import kotlinx.coroutines.flow.Flow

/**
 * Gestiona las sedes asignadas a cada usuario.
 *
 * El administrador puede asignar o quitar sedes.
 * El personal solo debe acceder a las sedes
 * que tiene asignadas.
 */
@Dao
interface UsuarioSedeDao {

    /**
     * Obtiene únicamente las sedes asignadas
     * al usuario indicado.
     *
     * Se utiliza para mostrar las sedes
     * autorizadas al trabajador.
     */
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

    /**
     * Obtiene las asignaciones actuales.
     *
     * Permite conocer qué sedes tiene
     * asignadas un trabajador.
     */
    @Query(
        "SELECT * FROM usuario_sede WHERE usuarioId = :usuarioId"
    )
    suspend fun getAsignacionesPorUsuario(
        usuarioId: Int
    ): List<UsuarioSede>

    /**
     * Asigna una sede a un usuario.
     *
     * IGNORE evita duplicar una asignación
     * que ya existe.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(
        asignacion: UsuarioSede
    )

    /**
     * Elimina todas las asignaciones
     * de sedes de un usuario.
     *
     * Se utiliza al actualizar las sedes
     * autorizadas por el administrador.
     */
    @Query(
        "DELETE FROM usuario_sede WHERE usuarioId = :usuarioId"
    )
    suspend fun deletePorUsuario(
        usuarioId: Int
    )
}
