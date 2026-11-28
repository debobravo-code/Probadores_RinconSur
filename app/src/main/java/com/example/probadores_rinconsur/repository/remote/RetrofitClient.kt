package com.example.probadores_rinconsur.repository.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Microservicio propio (Spring Boot + MySQL).
    private const val BASE_URL = "http://127.0.0.1:8080/"

    // API externa (Open-Meteo)
    private const val CLIMA_URL = "https://api.open-meteo.com/"

    // Las fotos se guardan en el servidor con rutas como "/api/fotos/archivo.jpg"
    const val PREFIJO_FOTOS = "/api/fotos/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val climaApiService: ClimaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(CLIMA_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApiService::class.java)
    }

    /**
     * Convierte la ruta de una foto del servidor en una URL completa para Coil.
     * Las fotos recién tomadas (archivo local) se dejan como están.
     */
    fun urlFoto(fotoUri: String?): String? =
        if (fotoUri != null && fotoUri.startsWith(PREFIJO_FOTOS)) {
            BASE_URL + fotoUri.removePrefix("/")
        } else {
            fotoUri
        }
}
