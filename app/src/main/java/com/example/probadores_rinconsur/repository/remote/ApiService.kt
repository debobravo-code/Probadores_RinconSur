package com.example.probadores_rinconsur.repository.remote

import com.example.probadores_rinconsur.repository.models.*
import com.example.probadores_rinconsur.repository.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // LOGIN

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<UserResponse>

    // SEDES

    @GET("api/sedes")
    suspend fun listarSedes(): Response<List<Sede>>

    @GET("api/sedes/{id}")
    suspend fun obtenerSede(
        @Path("id") id: Int
    ): Response<Sede>

    @POST("api/sedes")
    suspend fun crearSede(
        @Body sede: Sede
    ): Response<Sede>

    @PUT("api/sedes/{id}")
    suspend fun actualizarSede(
        @Path("id") id: Int,
        @Body sede: Sede
    ): Response<Sede>

    @DELETE("api/sedes/{id}")
    suspend fun eliminarSede(
        @Path("id") id: Int
    ): Response<Unit>

    // PROBADORES

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

    // USUARIOS

    @GET("api/users")
    suspend fun listarUsuarios(): Response<List<UserResponse>>

    @GET("api/users/{id}")
    suspend fun obtenerUsuario(
        @Path("id") id: Int
    ): Response<UserResponse>

    @POST("api/users")
    suspend fun crearUsuario(
        @Body user: User
    ): Response<UserResponse>

    @PUT("api/users/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Int,
        @Body user: User
    ): Response<UserResponse>

    @DELETE("api/users/{id}")
    suspend fun eliminarUsuario(
        @Path("id") id: Int
    ): Response<Unit>

    // TIPOS DE USUARIO

    @GET("api/tipos-usuario")
    suspend fun listarTiposUsuario(): Response<List<TipoUsuario>>

    @GET("api/tipos-usuario/{id}")
    suspend fun obtenerTipoUsuario(
        @Path("id") id: Int
    ): Response<TipoUsuario>

    @POST("api/tipos-usuario")
    suspend fun crearTipoUsuario(
        @Body tipo: TipoUsuario
    ): Response<TipoUsuario>

    @PUT("api/tipos-usuario/{id}")
    suspend fun actualizarTipoUsuario(
        @Path("id") id: Int,
        @Body tipo: TipoUsuario
    ): Response<TipoUsuario>

    @DELETE("api/tipos-usuario/{id}")
    suspend fun eliminarTipoUsuario(
        @Path("id") id: Int
    ): Response<Unit>

    // REGISTROS DE OCUPACION

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

    // REPORTES DE PRENDAS

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

    // FOTOGRAFIAS

    @Multipart
    @POST("api/fotos")
    suspend fun subirFoto(
        @Part archivo: okhttp3.MultipartBody.Part
    ): Response<Map<String, String>>

    @GET("api/fotos/{nombreArchivo}")
    suspend fun obtenerFoto(
        @Path("nombreArchivo") nombreArchivo: String
    ): Response<okhttp3.ResponseBody>

    // FILTRAR PROBADORES POR ESTADO

    @GET("api/probadores/sede/{sedeId}/estado/{estado}")
    suspend fun listarProbadoresPorEstado(
        @Path("sedeId") sedeId: Int,
        @Path("estado") estado: EstadoProbador
    ): Response<List<Probador>>

}