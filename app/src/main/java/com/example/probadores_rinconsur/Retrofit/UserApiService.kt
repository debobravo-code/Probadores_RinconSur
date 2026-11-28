package com.example.probadores_rinconsur.Retrofit

import com.example.probadores_rinconsur.repository.models.*
import com.example.probadores_rinconsur.Retrofit.dto.*
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

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

@GET("api/users/{id}/sedes")
    suspend fun listarSedesDeUsuario(
        @Path("id") id: Int
    ): Response<List<Sede>>

    @PUT("api/users/{id}/sedes")
    suspend fun asignarSedes(
        @Path("id") id: Int,
        @Body sedesIds: List<Int>
    ): Response<List<Sede>>
}