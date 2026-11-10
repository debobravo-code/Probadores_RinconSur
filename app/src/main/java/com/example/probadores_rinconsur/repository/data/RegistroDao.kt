
package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {

    // Historial: los usos más recientes primero
    @Query("SELECT * FROM registros ORDER BY inicioMillis DESC")
    fun getAll(): Flow<List<RegistroOcupacion>>

    // Guardar un nuevo registro de ocupación
    @Insert
    suspend fun insert(registro: RegistroOcupacion): Long

    // Actualizar un registro existente del historial
    @Update
    suspend fun update(registro: RegistroOcupacion)
}
