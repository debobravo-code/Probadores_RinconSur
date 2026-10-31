package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.probadores_rinconsur.repository.models.TipoUsuario
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoUsuarioDao {
    @Query("SELECT * FROM tipo_usuarios ORDER BY id ASC")
    fun getAll(): Flow<List<TipoUsuario>>

    @Query("SELECT * FROM tipo_usuarios WHERE id = :id")
    suspend fun getById(id: Int): TipoUsuario?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tipo: TipoUsuario): Long

    @Update
    suspend fun update(tipo: TipoUsuario)

    @Delete
    suspend fun delete(tipo: TipoUsuario)
}