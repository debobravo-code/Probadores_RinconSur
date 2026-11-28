package com.example.probadores_rinconsur.repository.remote.dto

/** Datos que se pueden corregir en un registro del historial. */
data class RegistroEdicionRequest(
    val cantidadPrendas: Int,
    val prendaOlvidada: Boolean,
    val observacion: String
)
