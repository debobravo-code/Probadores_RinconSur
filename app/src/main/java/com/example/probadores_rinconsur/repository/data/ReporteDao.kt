package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.probadores_rinconsur.repository.models.ReportePrenda
import kotlinx.coroutines.flow.Flow

/**
 * Copia local (Room) de la tabla "reportes_prenda" de MySQL.
 * Los ids son los mismos que en MySQL, así que no se duplican registros.
 */
@Dao
abstract class ReporteDao {

    // Lo que ve la pantalla: siempre se lee desde Room
    @Query("SELECT * FROM reportes_prenda ORDER BY fechaMillis DESC")
    abstract fun getAll(): Flow<List<ReportePrenda>>

    @Query("SELECT * FROM reportes_prenda WHERE id = :id")
    abstract suspend fun getById(id: Int): ReportePrenda?

    @Query("DELETE FROM reportes_prenda")
    abstract suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(lista: List<ReportePrenda>)

    /**
     * Deja la copia local igual a lo que hay en MySQL.
     * Va en una sola transacción: la pantalla nunca ve la tabla a medias.
     */
    @Transaction
    open suspend fun reemplazarTodo(lista: List<ReportePrenda>) {
        deleteAll()
        insertAll(lista)
    }
}
