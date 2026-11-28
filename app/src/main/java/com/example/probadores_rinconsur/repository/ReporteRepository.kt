package com.example.probadores_rinconsur.repository

import com.example.probadores_rinconsur.repository.data.ReporteDao
import com.example.probadores_rinconsur.repository.models.ReportePrenda
import com.example.probadores_rinconsur.repository.remote.ApiService
import com.example.probadores_rinconsur.repository.remote.RetrofitClient
import com.example.probadores_rinconsur.repository.remote.dto.ReportePrendaRequest
import com.example.probadores_rinconsur.RinconSurApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

/** Reportes de prenda olvidada: MySQL por Retrofit + copia local en Room. */
class ReporteRepository(
    private val api: ApiService = RetrofitClient.apiService,
    private val dao: ReporteDao = RinconSurApp.database.reporteDao()
) {

    val reportes: Flow<List<ReportePrenda>> = dao.getAll()

    suspend fun refrescar(): Boolean {
        return try {
            val respuesta = api.listarReportes()
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

    /** Pide el reporte al servidor; si no hay conexión usa la copia de Room. */
    suspend fun obtener(id: Int): ReportePrenda? {
        return try {
            val respuesta = api.obtenerReporte(id)
            if (respuesta.isSuccessful) respuesta.body() else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            dao.getById(id)
        }
    }

    /**
     * Sube la foto tomada con la cámara (archivo local) y devuelve la ruta del servidor.
     * Devuelve null si el archivo no existe o el servidor la rechaza.
     */
    suspend fun subirFoto(fotoLocal: String): String? {
        val archivo = File(fotoLocal.removePrefix("file://"))
        if (!archivo.exists()) return null

        val cuerpo = archivo.asRequestBody("image/jpeg".toMediaType())
        val parte = MultipartBody.Part.createFormData("archivo", archivo.name, cuerpo)

        val respuesta = api.subirFoto(parte)
        return if (respuesta.isSuccessful) respuesta.body()?.get("fotoUri") else null
    }

    suspend fun crear(request: ReportePrendaRequest): Response<ReportePrenda> =
        api.crearReporte(request).also { if (it.isSuccessful) refrescar() }

    suspend fun actualizar(id: Int, request: ReportePrendaRequest): Response<ReportePrenda> =
        api.actualizarReporte(id, request).also { if (it.isSuccessful) refrescar() }

    suspend fun eliminar(id: Int): Response<Unit> =
        api.eliminarReporte(id).also { if (it.isSuccessful) refrescar() }
}
