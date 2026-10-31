package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Resumen de cuántas prendas se prueba la gente. distribucion[0] = usos con 1 prenda ... [3] = usos con 4. */
data class ResumenPrendas(
    val usos: Int,
    val totalPrendas: Int,
    val promedio: Double,
    val distribucion: List<Int>
)

/** Solo lectura: calcula las estadísticas con los registros que guarda ProbadorViewModel al finalizar un uso. */
class EstadisticasViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    val registros: StateFlow<List<RegistroOcupacion>> = db.registroDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sedes: StateFlow<List<Sede>> = db.sedeDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** sedeId = 0 -> todas las sedes. Los registros sin prendas (cantidad 0) no cuentan. */
    fun resumen(registros: List<RegistroOcupacion>, sedeId: Int): ResumenPrendas {
        val validos = registros.filter {
            (sedeId == 0 || it.sedeId == sedeId) && it.cantidadPrendas in 1..Probador.MAX_PRENDAS
        }
        val total = validos.sumOf { it.cantidadPrendas }
        return ResumenPrendas(
            usos = validos.size,
            totalPrendas = total,
            promedio = if (validos.isEmpty()) 0.0 else total.toDouble() / validos.size,
            distribucion = (1..Probador.MAX_PRENDAS).map { n ->
                validos.count { it.cantidadPrendas == n }
            }
        )
    }
}