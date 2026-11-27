package com.example.probadores_rinconsur.repository.remote.dto

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val tipoUsuarioId: Int
)