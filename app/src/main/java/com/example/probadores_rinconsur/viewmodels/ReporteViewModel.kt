package com.example.probadores_rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.ProbadorRepository
import com.example.probadores_rinconsur.repository.ReporteRepository
import com.example.probadores_rinconsur.repository.SedeRepository
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.models.ReportePrenda
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.repository.remote.RetrofitClient
import com.example.probadores_rinconsur.repository.remote.dto.ReportePrendaRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SIN_CONEXION = "No se pudo conectar con el servidor"
private const val SIN_ACTUALIZAR =
    "No se pudo actualizar desde el servidor. Se muestran los datos guardados"

/**
 * Reportes de prendas olvidadas.
 * La foto se sube primero al servidor (POST /api/fotos) y el reporte
 * guarda la ruta que devuelve el servidor.
 */
class ReporteViewModel(
    private val reporteRepo: ReporteRepository = ReporteRepository(),
    private val probadorRepo: ProbadorRepository = ProbadorRepository(),
    private val sedeRepo: SedeRepository = SedeRepository()
) : ViewModel() {

    // READ: listas expuestas a la vista (vienen de Room)
    val reportes: StateFlow<List<ReportePrenda>> =
        reporteRepo.reportes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Probadores y sedes: para el selector del formulario y para mostrar nombres en la lista
    val probadores: StateFlow<List<Probador>> =
        probadorRepo.probadores.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val sedes: StateFlow<List<Sede>> =
        sedeRepo.sedes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        cargarDatos()
    }

    fun clearMessage() {
        _message.value = null
    }

    /** Vuelve a pedir reportes, probadores y sedes al servidor. */
    fun cargarDatos() {
        viewModelScope.launch {
            actualizarListados()
        }
    }

    private suspend fun actualizarListados() {
        // Si no hay conexión, Room conserva la última copia y se sigue viendo
        val reportesOk = reporteRepo.refrescar()
        val probadoresOk = probadorRepo.refrescar()
        val sedesOk = sedeRepo.refrescar()

        if (!reportesOk || !probadoresOk || !sedesOk) {
            _message.value = SIN_ACTUALIZAR
        }
    }

    // Lectura puntual (se invoca desde una corrutina de la vista: LaunchedEffect)
    suspend fun getReporte(id: Int): ReportePrenda? {
        return reporteRepo.obtener(id)
    }

    // CREATE (id = 0) / UPDATE
    // Si el probador seguía ocupado, el servidor finaliza su uso y lo deja en PRENDA_OLVIDADA.
    // La fecha la asigna el servidor, por eso no se envía.
    fun saveReporte(
        id: Int,
        probadorId: Int,
        descripcion: String,
        fotoUri: String?
    ) {
        viewModelScope.launch {
            try {
                // Una foto que todavía no está en el servidor se sube antes de guardar el reporte
                var fotoFinal = fotoUri

                if (fotoUri != null && !fotoUri.startsWith(RetrofitClient.PREFIJO_FOTOS)) {
                    fotoFinal = reporteRepo.subirFoto(fotoUri)

                    if (fotoFinal == null) {
                        _message.value = "No se pudo subir la fotografía"
                        return@launch
                    }
                }

                val request = ReportePrendaRequest(
                    probadorId = probadorId,
                    descripcion = descripcion,
                    fotoUri = fotoFinal
                )

                val respuesta = if (id == 0) {
                    reporteRepo.crear(request)
                } else {
                    reporteRepo.actualizar(id, request)
                }

                if (respuesta.isSuccessful) {
                    _message.value = null
                    // Si el probador cambió de estado en el servidor, se actualiza su copia
                    probadorRepo.refrescar()
                } else {
                    _message.value = when (respuesta.code()) {
                        400 -> "Los datos del reporte no son válidos"
                        404 -> "Reporte no encontrado"
                        else -> "Error del servidor: ${respuesta.code()}"
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _message.value = SIN_CONEXION
            }
        }
    }

    // DELETE
    fun deleteReporte(reporte: ReportePrenda) {
        viewModelScope.launch {
            try {
                val respuesta = reporteRepo.eliminar(reporte.id)

                if (respuesta.isSuccessful) {
                    _message.value = null
                } else {
                    _message.value = when (respuesta.code()) {
                        404 -> "Reporte no encontrado"
                        else -> "Error del servidor: ${respuesta.code()}"
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _message.value = SIN_CONEXION
            }
        }
    }
}
