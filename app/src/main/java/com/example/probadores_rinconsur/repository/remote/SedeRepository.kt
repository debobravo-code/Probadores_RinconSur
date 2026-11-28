package com.example.probadores_rinconsur.repository.remote

import com.example.probadores_rinconsur.repository.data.SedeDao
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.Retrofit.SedeApiService
import com.example.probadores_rinconsur.Retrofit.RetrofitClient
import com.example.probadores_rinconsur.Retrofit.ProbadorApiService
import com.example.probadores_rinconsur.Retrofit.dto.SedeRequest
import com.example.probadores_rinconsur.RinconSurApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

/**
 * Sedes: MySQL (por Retrofit) es la fuente de verdad y Room es la copia local.
 * - La pantalla lee siempre desde Room (sedes).
 * - refrescar() baja la lista de MySQL y reemplaza la copia de Room.
 * - Cada cambio (crear, editar, eliminar) se guarda primero en MySQL y,
 *   si salió bien, se actualiza Room. Así no se duplican ni se pierden datos.
 */
class SedeRepository(
    private val api: SedeApiService = RetrofitClient.sedeApiService,
    private val dao: SedeDao = RinconSurApp.database.sedeDao(),
    private val probadorApi: ProbadorApiService = RetrofitClient.probadorApiService
) {

    val sedes: Flow<List<Sede>> = dao.getAll()

    /** Devuelve false si no se pudo hablar con el servidor (Room queda como estaba). */
    suspend fun refrescar(): Boolean {
        return try {
            val respuesta = api.listarSedes()
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

    suspend fun crear(request: SedeRequest): Response<Sede> =
        api.crearSede(request).also { if (it.isSuccessful) refrescar() }

    suspend fun actualizar(id: Int, request: SedeRequest): Response<Sede> =
        api.actualizarSede(id, request).also { if (it.isSuccessful) refrescar() }

    suspend fun eliminar(id: Int): Response<Unit> =
        api.eliminarSede(id).also { if (it.isSuccessful) refrescar() }

    /** Probadores de una sede (se consulta al servidor). */
    suspend fun probadoresDeSede(sedeId: Int): Response<List<Probador>> =
        probadorApi.listarProbadoresPorSede(sedeId)
}
