package com.example.probadores_rinconsur.Retrofit

import com.example.probadores_rinconsur.repository.models.*
import com.example.probadores_rinconsur.Retrofit.dto.*
import retrofit2.Response
import retrofit2.http.*

interface FotoApiService {

    @Multipart
    @POST("api/fotos")
    suspend fun subirFoto(
        @Part archivo: okhttp3.MultipartBody.Part
    ): Response<Map<String, String>>

    @GET("api/fotos/{nombreArchivo}")
    suspend fun obtenerFoto(
        @Path("nombreArchivo") nombreArchivo: String
    ): Response<okhttp3.ResponseBody>
}