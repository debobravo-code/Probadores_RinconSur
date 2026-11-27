package com.example.probadores_rinconsur.repository.remote.dto

data class ReportePrendaRequest(
    val probadorId: Int,
    val descripcion: String,
    val fotoUri: String? = null
)