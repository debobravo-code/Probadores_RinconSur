
package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel del historial.
 * Permite consultar y editar registros de ocupación.
 * Los registros nuevos los crea ProbadorViewModel.
 */
class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    // DAO encargado de consultar y actualizar los registros
    private val registroDao = db.registroDao()

    // Lista de registros del historial
    val registros: StateFlow<List<RegistroOcupacion>> =
        registroDao.getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // Lista de sedes
    val sedes: StateFlow<List<Sede>> =
        db.sedeDao().getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // Actualizar un registro existente del historial
    fun actualizarRegistro(registro: RegistroOcupacion) {
        viewModelScope.launch(Dispatchers.IO) {
            registroDao.update(registro)
        }
    }
}
