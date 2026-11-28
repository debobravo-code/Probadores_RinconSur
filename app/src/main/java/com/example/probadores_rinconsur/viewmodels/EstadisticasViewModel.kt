package com.example.probadores_rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.RegistroRepository
import com.example.probadores_rinconsur.repository.SedeRepository
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Resumen de cuántas prendas se prueba la gente. distribucion[0] = usos con 1 prenda ... [3] = usos con 4. */
data class ResumenPrendas(
    val usos: Int,
    val totalPrendas: Int,
    val promedio: Double,
    val distribucion: List<Int>
)

/** Solo lectura: calcula las estadísticas con los registros (copia en Room de los que guarda el servidor). */
class EstadisticasViewModel(
    private val registroRepo: RegistroRepository = RegistroRepository(),
    private val sedeRepo: SedeRepository = SedeRepository()
) : ViewModel() {

    val registros: StateFlow<List<RegistroOcupacion>> =
        registroRepo.registros.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val sedes: StateFlow<List<Sede>> =
        sedeRepo.sedes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        cargarDatos()
    }

    /** Actualiza Room desde el servidor. Si no hay conexión se usan los datos guardados. */
    fun cargarDatos() {
        viewModelScope.launch {
            val registrosOk = registroRepo.refrescar()
            val sedesOk = sedeRepo.refrescar()

            _message.value = if (registrosOk && sedesOk) {
                null
            } else {
                "No se pudo actualizar desde el servidor. Se muestran los datos guardados"
            }
        }
    }

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
