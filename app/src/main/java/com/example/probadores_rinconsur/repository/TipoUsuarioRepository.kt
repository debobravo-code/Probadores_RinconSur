package com.example.probadores_rinconsur.repository

import com.example.probadores_rinconsur.repository.data.TipoUsuarioDao
import com.example.probadores_rinconsur.repository.models.TipoUsuario
import com.example.probadores_rinconsur.repository.remote.ApiService
import com.example.probadores_rinconsur.repository.remote.RetrofitClient
import com.example.probadores_rinconsur.RinconSurApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

/** Tipos de usuario: MySQL por Retrofit + copia local en Room (misma idea que SedeRepository). */
class TipoUsuarioRepository(
    private val api: ApiService = RetrofitClient.apiService,
    private val dao: TipoUsuarioDao = RinconSurApp.database.tipoUsuarioDao()
) {

    val tipos: Flow<List<TipoUsuario>> = dao.getAll()

    suspend fun refrescar(): Boolean {
        return try {
            val respuesta = api.listarTiposUsuario()
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

    suspend fun crear(tipo: TipoUsuario): Response<TipoUsuario> =
        api.crearTipoUsuario(tipo).also { if (it.isSuccessful) refrescar() }

    suspend fun actualizar(id: Int, tipo: TipoUsuario): Response<TipoUsuario> =
        api.actualizarTipoUsuario(id, tipo).also { if (it.isSuccessful) refrescar() }

    suspend fun eliminar(id: Int): Response<Unit> =
        api.eliminarTipoUsuario(id).also { if (it.isSuccessful) refrescar() }
}
