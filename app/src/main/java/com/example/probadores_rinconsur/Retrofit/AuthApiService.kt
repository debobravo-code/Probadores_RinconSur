package com.example.probadores_rinconsur.Retrofit

import com.example.probadores_rinconsur.repository.models.*
import com.example.probadores_rinconsur.Retrofit.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<UserResponse>
}