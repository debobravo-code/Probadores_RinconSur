package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.EstadoProbador
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.ReportePrenda
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReporteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val reporteDao = db.reporteDao()
    private val probadorDao = db.probadorDao()
    private val registroDao = db.registroDao()

    // READ: lista reactiva expuesta a la vista
    val reportes: StateFlow<List<ReportePrenda>> = reporteDao.getAllReportes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Probadores y sedes: para el selector del formulario y para mostrar nombres en la lista
    val probadores: StateFlow<List<Probador>> = probadorDao.getAllProbadores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sedes: StateFlow<List<Sede>> = db.sedeDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lectura puntual (se invoca desde una corrutina de la vista: LaunchedEffect)
    suspend fun getReporte(id: Int): ReportePrenda? = withContext(Dispatchers.IO) {
        reporteDao.getReporteById(id)
    }

    // CREATE / UPDATE
    fun saveReporte(
        id: Int,
        probadorId: Int,
        descripcion: String,
        fotoUri: String?,
        fechaMillis: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            db.withTransaction {
                if (id == 0) {
                    // Si el probador seguía ocupado, el reporte también finaliza su uso
                    // (queda PRENDA_OLVIDADA)
                    val p = probadorDao.getProbadorById(probadorId)
                    val inicio = p?.inicioOcupacion

                    if (
                        p != null &&
                        p.estado == EstadoProbador.OCUPADO &&
                        inicio != null
                    ) {
                        registroDao.insert(
                            RegistroOcupacion(
                                sedeId = p.sedeId,
                                numeroProbador = p.numero,
                                inicioMillis = inicio,
                                finMillis = System.currentTimeMillis(),
                                prendaOlvidada = true,
                                cantidadPrendas = p.cantidadPrendas
                            )
                        )

                        probadorDao.updateProbador(
                            p.copy(
                                estado = EstadoProbador.PRENDA_OLVIDADA,
                                inicioOcupacion = null,
                                cantidadPrendas = 0
                            )
                        )
                    }

                    reporteDao.insertReporte(
                        ReportePrenda(
                            probadorId = probadorId,
                            descripcion = descripcion,
                            fotoUri = fotoUri,
                            fechaMillis = System.currentTimeMillis()
                        )
                    )
                } else {
                    reporteDao.updateReporte(
                        ReportePrenda(
                            id = id,
                            probadorId = probadorId,
                            descripcion = descripcion,
                            fotoUri = fotoUri,
                            fechaMillis = fechaMillis
                        )
                    )
                }
            }
        }
    }

    // DELETE
    fun deleteReporte(reporte: ReportePrenda) {
        viewModelScope.launch(Dispatchers.IO) {
            reporteDao.deleteReporte(reporte)
        }
    }
}