package com.example.probadores_rinconsur.viewmodels

import com.example.probadores_rinconsur.repository.remote.SedeRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Prueba 1 (ViewModel): una sede con un nombre inválido no se guarda
 * y la pantalla recibe un mensaje de error.
 */
class SedeViewModelTest : StringSpec({

    // El ViewModel usa viewModelScope (Dispatchers.Main), por eso se reemplaza en la prueba
    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    "un nombre con números no se guarda y muestra un mensaje de error" {
        // 1. Repositorio simulado: no usa servidor ni base de datos
        val repo = mockk<SedeRepository>()
        every { repo.sedes } returns flowOf(emptyList())
        coEvery { repo.refrescar() } returns true

        // 2. ViewModel real, conectado al repositorio simulado
        val viewModel = SedeViewModel(repo)

        // 3. Se intenta guardar una sede con un nombre que tiene números
        viewModel.save(0, "Sede 123", "15", "3")

        // 4. Se muestra el mensaje y nunca se llamó al repositorio para crear la sede
        viewModel.message.value shouldBe
            "El nombre solo puede contener letras y un espacio entre palabras"
        coVerify(exactly = 0) { repo.crear(any()) }
    }
})
