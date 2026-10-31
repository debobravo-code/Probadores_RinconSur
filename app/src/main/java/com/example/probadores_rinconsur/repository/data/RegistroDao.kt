package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {

    // Historial: los usos más recientes primero
    @Query("SELECT * FROM registros ORDER BY inicioMillis DESC")
    fun getAll(): Flow<List<RegistroOcupacion>>

    @Insert
    suspend fun insert(registro: RegistroOcupacion): Long
}