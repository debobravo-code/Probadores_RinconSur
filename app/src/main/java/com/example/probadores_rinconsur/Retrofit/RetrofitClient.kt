package com.example.probadores_rinconsur.Retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://127.0.0.1:8080/"
    private const val CLIMA_URL = "https://api.open-meteo.com/"

    const val PREFIJO_FOTOS = "/api/fotos/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val sedeApiService: SedeApiService by lazy {
        retrofit.create(SedeApiService::class.java)
    }

    val probadorApiService: ProbadorApiService by lazy {
        retrofit.create(ProbadorApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val tipoUsuarioApiService: TipoUsuarioApiService by lazy {
        retrofit.create(TipoUsuarioApiService::class.java)
    }

    val registroApiService: RegistroApiService by lazy {
        retrofit.create(RegistroApiService::class.java)
    }

    val reporteApiService: ReporteApiService by lazy {
        retrofit.create(ReporteApiService::class.java)
    }

    val fotoApiService: FotoApiService by lazy {
        retrofit.create(FotoApiService::class.java)
    }

    val climaApiService: ClimaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(CLIMA_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApiService::class.java)
    }

    fun urlFoto(fotoUri: String?): String? =
        if (fotoUri != null && fotoUri.startsWith(PREFIJO_FOTOS)) {
            BASE_URL + fotoUri.removePrefix("/")
        } else {
            fotoUri
        }
}
