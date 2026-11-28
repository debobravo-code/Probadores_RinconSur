package com.example.probadores_rinconsur.Retrofit

import com.example.probadores_rinconsur.repository.models.*
import com.example.probadores_rinconsur.Retrofit.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ProbadorApiService {

    @GET("api/probadores")
    suspend fun listarProbadores(): Response<List<Probador>>

    @GET("api/probadores/{id}")
    suspend fun obtenerProbador(
        @Path("id") id: Int
    ): Response<Probador>

    @GET("api/probadores/sede/{sedeId}")
    suspend fun listarProbadoresPorSede(
        @Path("sedeId") sedeId: Int
    ): Response<List<Probador>>

    @POST("api/probadores")
    suspend fun crearProbador(
        @Body probador: Probador
    ): Response<Probador>

    @PUT("api/probadores/{id}")
    suspend fun actualizarProbador(
        @Path("id") id: Int,
        @Body probador: Probador
    ): Response<Probador>

    @DELETE("api/probadores/{id}")
    suspend fun eliminarProbador(
        @Path("id") id: Int
    ): Response<Unit>

    @PATCH("api/probadores/{id}/ocupar")
    suspend fun ocuparProbador(
        @Path("id") id: Int,
        @Body request: OcupacionRequest
    ): Response<Probador>

    @PATCH("api/probadores/{id}/liberar")
    suspend fun liberarProbador(
        @Path("id") id: Int
    ): Response<Probador>

    @PATCH("api/probadores/{id}/estado")
    suspend fun cambiarEstadoProbador(
        @Path("id") id: Int,
        @Body request: EstadoProbadorRequest
    ): Response<Probador>

@GET("api/probadores/sede/{sedeId}/estado/{estado}")
    suspend fun listarProbadoresPorEstado(
        @Path("sedeId") sedeId: Int,
        @Path("estado") estado: EstadoProbador
    ): Response<List<Probador>>
}