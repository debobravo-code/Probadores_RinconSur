package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.probadores_rinconsur.repository.models.ReportePrenda
import kotlinx.coroutines.flow.Flow

@Dao
interface ReporteDao {
    // READ (reactivo con Flow)
    @Query("SELECT * FROM reportes_prenda ORDER BY fechaMillis DESC")
    fun getAllReportes(): Flow<List<ReportePrenda>>

    @Query("SELECT * FROM reportes_prenda WHERE id = :id")
    suspend fun getReporteById(id: Int): ReportePrenda?

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReporte(reporte: ReportePrenda): Long

    // UPDATE
    @Update
    suspend fun updateReporte(reporte: ReportePrenda)

    // DELETE
    @Delete
    suspend fun deleteReporte(reporte: ReportePrenda)
}