package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.probadores_rinconsur.repository.models.User
import kotlinx.coroutines.flow.Flow

/**
 * Copia local (Room) de la tabla "users" de MySQL.
 * Los ids son los mismos que en MySQL, así que no se duplican registros.
 */
@Dao
abstract class UserDao {

    // Lo que ve la pantalla: siempre se lee desde Room
    @Query("SELECT * FROM users ORDER BY name ASC")
    abstract fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    abstract suspend fun getById(id: Int): User?

    @Query("DELETE FROM users")
    abstract suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(lista: List<User>)

    /**
     * Deja la copia local igual a lo que hay en MySQL.
     * Va en una sola transacción: la pantalla nunca ve la tabla a medias.
     */
    @Transaction
    open suspend fun reemplazarTodo(lista: List<User>) {
        deleteAll()
        insertAll(lista)
    }
}
