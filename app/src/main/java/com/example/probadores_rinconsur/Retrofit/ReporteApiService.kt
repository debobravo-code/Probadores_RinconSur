package com.example.probadores_rinconsur.Retrofit

import com.example.probadores_rinconsur.repository.models.*
import com.example.probadores_rinconsur.Retrofit.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ReporteApiService {

    @GET("api/reportes-prenda")
    suspend fun listarReportes(): Response<List<ReportePrenda>>

    @GET("api/reportes-prenda/{id}")
    suspend fun obtenerReporte(
        @Path("id") id: Int
    ): Response<ReportePrenda>

    @GET("api/reportes-prenda/probador/{probadorId}")
    suspend fun listarReportesPorProbador(
        @Path("probadorId") probadorId: Int
    ): Response<List<ReportePrenda>>

    @GET("api/reportes-prenda/fechas")
    suspend fun listarReportesPorFechas(
        @Query("desde") desde: Long,
        @Query("hasta") hasta: Long
    ): Response<List<ReportePrenda>>

    @POST("api/reportes-prenda")
    suspend fun crearReporte(
        @Body request: ReportePrendaRequest
    ): Response<ReportePrenda>

    @PUT("api/reportes-prenda/{id}")
    suspend fun actualizarReporte(
        @Path("id") id: Int,
        @Body request: ReportePrendaRequest
    ): Response<ReportePrenda>

    @DELETE("api/reportes-prenda/{id}")
    suspend fun eliminarReporte(
        @Path("id") id: Int
    ): Response<Unit>
}