package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import kotlinx.coroutines.flow.Flow

/**
 * Copia local (Room) de la tabla "registros" de MySQL.
 * Los ids son los mismos que en MySQL, así que no se duplican registros.
 */
@Dao
abstract class RegistroDao {

    // Lo que ve la pantalla: siempre se lee desde Room
    @Query("SELECT * FROM registros ORDER BY inicioMillis DESC")
    abstract fun getAll(): Flow<List<RegistroOcupacion>>

    @Query("SELECT * FROM registros WHERE id = :id")
    abstract suspend fun getById(id: Int): RegistroOcupacion?

    @Query("DELETE FROM registros")
    abstract suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(lista: List<RegistroOcupacion>)

    /**
     * Deja la copia local igual a lo que hay en MySQL.
     * Va en una sola transacción: la pantalla nunca ve la tabla a medias.
     */
    @Transaction
    open suspend fun reemplazarTodo(lista: List<RegistroOcupacion>) {
        deleteAll()
        insertAll(lista)
    }
}
