package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.probadores_rinconsur.repository.models.TipoUsuario
import kotlinx.coroutines.flow.Flow

/**
 * Copia local (Room) de la tabla "tipo_usuarios" de MySQL.
 * Los ids son los mismos que en MySQL, así que no se duplican registros.
 */
@Dao
abstract class TipoUsuarioDao {

    // Lo que ve la pantalla: siempre se lee desde Room
    @Query("SELECT * FROM tipo_usuarios ORDER BY id ASC")
    abstract fun getAll(): Flow<List<TipoUsuario>>

    @Query("SELECT * FROM tipo_usuarios WHERE id = :id")
    abstract suspend fun getById(id: Int): TipoUsuario?

    @Query("DELETE FROM tipo_usuarios")
    abstract suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(lista: List<TipoUsuario>)

    /**
     * Deja la copia local igual a lo que hay en MySQL.
     * Va en una sola transacción: la pantalla nunca ve la tabla a medias.
     */
    @Transaction
    open suspend fun reemplazarTodo(lista: List<TipoUsuario>) {
        deleteAll()
        insertAll(lista)
    }
}
