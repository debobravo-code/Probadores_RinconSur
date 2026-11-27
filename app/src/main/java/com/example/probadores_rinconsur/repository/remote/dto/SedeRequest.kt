package com.example.probadores_rinconsur.repository.remote.dto

data class SedeRequest(
    val nombre: String,
    val tiempoAlerta: Int,
    val cantidadProbadores: Int
)