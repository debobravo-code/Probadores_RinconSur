package com.example.probadores_rinconsur.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reportes_prenda")
data class ReportePrenda(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sedeId: Int,
    val numeroProbador: Int,
    val descripcion: String,
    val fotoUri: String? = null,
    val fechaMillis: Long = System.currentTimeMillis()
)