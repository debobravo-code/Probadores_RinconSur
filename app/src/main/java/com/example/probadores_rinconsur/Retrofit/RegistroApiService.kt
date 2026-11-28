package com.example.probadores_rinconsur.Retrofit

import com.example.probadores_rinconsur.repository.models.*
import com.example.probadores_rinconsur.Retrofit.dto.*
import retrofit2.Response
import retrofit2.http.*

interface RegistroApiService {

    @GET("api/registros")
    suspend fun listarRegistros(): Response<List<RegistroOcupacion>>

    @GET("api/registros/{id}")
    suspend fun obtenerRegistro(
        @Path("id") id: Int
    ): Response<RegistroOcupacion>

    @GET("api/registros/sede/{sedeId}")
    suspend fun listarRegistrosPorSede(
        @Path("sedeId") sedeId: Int
    ): Response<List<RegistroOcupacion>>

    @GET("api/registros/sede/{sedeId}/probador/{numero}")
    suspend fun listarRegistrosPorProbador(
        @Path("sedeId") sedeId: Int,
        @Path("numero") numero: Int
    ): Response<List<RegistroOcupacion>>

    @GET("api/registros/sede/{sedeId}/prenda-olvidada")
    suspend fun listarRegistrosPorPrendaOlvidada(
        @Path("sedeId") sedeId: Int,
        @Query("valor") valor: Boolean
    ): Response<List<RegistroOcupacion>>

    @GET("api/registros/sede/{sedeId}/fechas")
    suspend fun listarRegistrosPorFechas(
        @Path("sedeId") sedeId: Int,
        @Query("desde") desde: Long,
        @Query("hasta") hasta: Long
    ): Response<List<RegistroOcupacion>>

    @PUT("api/registros/{id}")
    suspend fun actualizarRegistro(
        @Path("id") id: Int,
        @Body request: RegistroEdicionRequest
    ): Response<RegistroOcupacion>
}