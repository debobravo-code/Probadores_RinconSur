package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.probadores_rinconsur.repository.models.Probador
import kotlinx.coroutines.flow.Flow

@Dao
interface ProbadorDao {

    @Query("SELECT * FROM probadores ORDER BY numero ASC")
    fun getAllProbadores(): Flow<List<Probador>>

    @Query("SELECT * FROM probadores WHERE sedeId = :sedeId ORDER BY numero ASC")
    fun getProbadoresBySede(sedeId: Int): Flow<List<Probador>>

    @Query("SELECT * FROM probadores WHERE id = :id")
    suspend fun getProbadorById(id: Int): Probador?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProbador(probador: Probador): Long

    @Update
    suspend fun updateProbador(probador: Probador)

    @Delete
    suspend fun deleteProbador(probador: Probador)
}