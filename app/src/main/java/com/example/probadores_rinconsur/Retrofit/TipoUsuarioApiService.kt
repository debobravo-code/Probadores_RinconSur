package com.example.probadores_rinconsur.Retrofit

import com.example.probadores_rinconsur.repository.models.*
import com.example.probadores_rinconsur.Retrofit.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TipoUsuarioApiService {

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
}