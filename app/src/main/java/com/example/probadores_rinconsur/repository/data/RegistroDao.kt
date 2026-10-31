package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {

    @Query("SELECT * FROM registros ORDER BY finMillis DESC")
    fun getAllRegistros(): Flow<List<RegistroOcupacion>>

    @Query("SELECT * FROM registros WHERE sedeId = :sedeId ORDER BY finMillis DESC")
    fun getRegistrosBySede(sedeId: Int): Flow<List<RegistroOcupacion>>

    @Insert
    suspend fun insertRegistro(registro: RegistroOcupacion): Long

    @Query("DELETE FROM registros")
    suspend fun deleteAll()
}