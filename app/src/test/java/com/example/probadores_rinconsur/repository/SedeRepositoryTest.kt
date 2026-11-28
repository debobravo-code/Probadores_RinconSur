package com.example.probadores_rinconsur.repository

import com.example.probadores_rinconsur.repository.data.SedeDao
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.Retrofit.SedeApiService
import com.example.probadores_rinconsur.Retrofit.ProbadorApiService
import com.example.probadores_rinconsur.repository.remote.SedeRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import retrofit2.Response

/**
 * Prueba 2 (Repository): al refrescar, la lista que entrega el servidor (MySQL)
 * se guarda en Room, que es la copia local.
 */
class SedeRepositoryTest : StringSpec({

    "refrescar guarda en Room la lista que entrega el servidor" {
        val sedes = listOf(
            Sede(1, "Santiago Norte", 15),
            Sede(2, "Santiago Centro", 15)
        )

        // 1. Servidor y Room simulados
        val api = mockk<SedeApiService>()
        val dao = mockk<SedeDao>()
        every { dao.getAll() } returns flowOf(sedes)
        coEvery { api.listarSedes() } returns Response.success(sedes)
        coEvery { dao.reemplazarTodo(sedes) } returns Unit

        // 2. Repositorio real, conectado a los simulados
        val repository = SedeRepository(api, dao, mockk<ProbadorApiService>())

        // 3. Se actualiza la copia local desde el servidor
        val resultado = repository.refrescar()

        // 4. Salió bien y la lista se guardó en Room
        resultado shouldBe true
        coVerify(exactly = 1) { dao.reemplazarTodo(sedes) }
    }
})
