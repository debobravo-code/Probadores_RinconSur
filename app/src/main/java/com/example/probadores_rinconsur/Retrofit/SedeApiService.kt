package com.example.probadores_rinconsur.Retrofit

import com.example.probadores_rinconsur.repository.models.*
import com.example.probadores_rinconsur.Retrofit.dto.*
import retrofit2.Response
import retrofit2.http.*

interface SedeApiService {

    @GET("api/sedes")
    suspend fun listarSedes(): Response<List<Sede>>

    @GET("api/sedes/{id}")
    suspend fun obtenerSede(
        @Path("id") id: Int
    ): Response<Sede>

    @POST("api/sedes")
    suspend fun crearSede(
        @Body sede: SedeRequest
    ): Response<Sede>

    @PUT("api/sedes/{id}")
    suspend fun actualizarSede(
        @Path("id") id: Int,
        @Body sede: SedeRequest
    ): Response<Sede>

    @DELETE("api/sedes/{id}")
    suspend fun eliminarSede(
        @Path("id") id: Int
    ): Response<Unit>
}