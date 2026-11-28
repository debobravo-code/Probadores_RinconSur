package com.example.probadores_rinconsur.Retrofit.dto

import com.google.gson.annotations.SerializedName

/** Respuesta de Open-Meteo: solo se usa el bloque "current". */
data class ClimaResponse(
    val current: ClimaActual
)

data class ClimaActual(
    @SerializedName("temperature_2m") val temperatura: Double,
    @SerializedName("weather_code") val codigoClima: Int
)
