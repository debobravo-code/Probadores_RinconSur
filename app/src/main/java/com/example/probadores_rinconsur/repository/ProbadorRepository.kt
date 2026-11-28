package com.example.probadores_rinconsur.repository

import com.example.probadores_rinconsur.repository.data.ProbadorDao
import com.example.probadores_rinconsur.repository.models.EstadoProbador
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.remote.ApiService
import com.example.probadores_rinconsur.repository.remote.RetrofitClient
import com.example.probadores_rinconsur.repository.remote.dto.EstadoProbadorRequest
import com.example.probadores_rinconsur.repository.remote.dto.OcupacionRequest
import com.example.probadores_rinconsur.RinconSurApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

/** Probadores: MySQL por Retrofit + copia local en Room (misma idea que SedeRepository). */
class ProbadorRepository(
    private val api: ApiService = RetrofitClient.apiService,
    private val dao: ProbadorDao = RinconSurApp.database.probadorDao()
) {

    val probadores: Flow<List<Probador>> = dao.getAll()

    suspend fun refrescar(): Boolean {
        return try {
            val respuesta = api.listarProbadores()
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

    /** Pide el probador al servidor; si no hay conexión usa la copia de Room. */
    suspend fun obtener(id: Int): Probador? {
        return try {
            val respuesta = api.obtenerProbador(id)
            if (respuesta.isSuccessful) respuesta.body() else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            dao.getById(id)
        }
    }

    suspend fun crear(probador: Probador): Response<Probador> =
        api.crearProbador(probador).also { if (it.isSuccessful) refrescar() }

    suspend fun actualizar(id: Int, probador: Probador): Response<Probador> =
        api.actualizarProbador(id, probador).also { if (it.isSuccessful) refrescar() }

    suspend fun eliminar(id: Int): Response<Unit> =
        api.eliminarProbador(id).also { if (it.isSuccessful) refrescar() }

    suspend fun ocupar(id: Int, cantidadPrendas: Int): Response<Probador> =
        api.ocuparProbador(id, OcupacionRequest(cantidadPrendas)).also { if (it.isSuccessful) refrescar() }

    suspend fun liberar(id: Int): Response<Probador> =
        api.liberarProbador(id).also { if (it.isSuccessful) refrescar() }

    suspend fun cambiarEstado(id: Int, estado: EstadoProbador): Response<Probador> =
        api.cambiarEstadoProbador(id, EstadoProbadorRequest(estado)).also { if (it.isSuccessful) refrescar() }
}
