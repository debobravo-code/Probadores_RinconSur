package com.example.probadores_rinconsur.repository

import com.example.probadores_rinconsur.repository.data.RegistroDao
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.remote.ApiService
import com.example.probadores_rinconsur.repository.remote.RetrofitClient
import com.example.probadores_rinconsur.repository.remote.dto.RegistroEdicionRequest
import com.example.probadores_rinconsur.RinconSurApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

/**
 * Historial de ocupación. Los registros los crea el servidor al finalizar un uso;
 * la app solo los lee y puede editar cantidad de prendas, prenda olvidada y observación.
 */
class RegistroRepository(
    private val api: ApiService = RetrofitClient.apiService,
    private val dao: RegistroDao = RinconSurApp.database.registroDao()
) {

    val registros: Flow<List<RegistroOcupacion>> = dao.getAll()

    suspend fun refrescar(): Boolean {
        return try {
            val respuesta = api.listarRegistros()
            if (respuesta.isSuccessful) {
                dao.reemplazarTodo(respuesta.body().orEmpty())
                true
            } else {
                false
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    suspend fun actualizar(id: Int, request: RegistroEdicionRequest): Response<RegistroOcupacion> =
        api.actualizarRegistro(id, request).also { if (it.isSuccessful) refrescar() }
}
