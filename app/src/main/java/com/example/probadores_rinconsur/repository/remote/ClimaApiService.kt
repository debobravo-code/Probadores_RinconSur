package com.example.probadores_rinconsur.repository.remote

import com.example.probadores_rinconsur.repository.remote.dto.ClimaResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API EXTERNA: Open-Meteo (https://open-meteo.com), pública y sin clave.
 * Entrega la temperatura actual para mostrarla en la pantalla de inicio.
 */
interface ClimaApiService {

    @GET("v1/forecast")
    suspend fun obtenerClima(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") actual: String,
        @Query("timezone") zonaHoraria: String
    ): Response<ClimaResponse>
}
