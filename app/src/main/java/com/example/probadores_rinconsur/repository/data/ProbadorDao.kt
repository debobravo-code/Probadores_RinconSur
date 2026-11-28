package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.probadores_rinconsur.repository.models.Probador
import kotlinx.coroutines.flow.Flow

/**
 * Copia local (Room) de la tabla "probadores" de MySQL.
 * Los ids son los mismos que en MySQL, así que no se duplican registros.
 */
@Dao
abstract class ProbadorDao {

    // Lo que ve la pantalla: siempre se lee desde Room
    @Query("SELECT * FROM probadores ORDER BY numero ASC")
    abstract fun getAll(): Flow<List<Probador>>

    @Query("SELECT * FROM probadores WHERE id = :id")
    abstract suspend fun getById(id: Int): Probador?

    @Query("DELETE FROM probadores")
    abstract suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(lista: List<Probador>)

    /**
     * Deja la copia local igual a lo que hay en MySQL.
     * Va en una sola transacción: la pantalla nunca ve la tabla a medias.
     */
    @Transaction
    open suspend fun reemplazarTodo(lista: List<Probador>) {
        deleteAll()
        insertAll(lista)
    }
}
