package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.flow.Flow

@Dao
interface SedeDao {
    @Query("SELECT * FROM sedes ORDER BY id ASC")
    fun getAll(): Flow<List<Sede>>

    @Query("SELECT * FROM sedes WHERE id = :id")
    suspend fun getById(id: Int): Sede?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sede: Sede): Long

    @Update
    suspend fun update(sede: Sede)

    @Delete
    suspend fun delete(sede: Sede)
}