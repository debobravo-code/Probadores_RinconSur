package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.flow.Flow

/**
 * Copia local (Room) de la tabla "sedes" de MySQL.
 * Los ids son los mismos que en MySQL, así que no se duplican registros.
 */
@Dao
abstract class SedeDao {

    // Lo que ve la pantalla: siempre se lee desde Room
    @Query("SELECT * FROM sedes ORDER BY id ASC")
    abstract fun getAll(): Flow<List<Sede>>

    @Query("SELECT * FROM sedes WHERE id = :id")
    abstract suspend fun getById(id: Int): Sede?

    @Query("DELETE FROM sedes")
    abstract suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(lista: List<Sede>)

    /**
     * Deja la copia local igual a lo que hay en MySQL.
     * Va en una sola transacción: la pantalla nunca ve la tabla a medias.
     */
    @Transaction
    open suspend fun reemplazarTodo(lista: List<Sede>) {
        deleteAll()
        insertAll(lista)
    }
}
