package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SedeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).sedeDao()

    val sedes: StateFlow<List<Sede>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    /** CREATE (id = 0) o UPDATE (id > 0). */
    fun save(id: Int, nombre: String, tiempoAlerta: String, onSuccess: () -> Unit = {}) {
        val valor = nombre.trim()
        if (valor.isEmpty() || valor.length > Sede.MAX_LARGO) {
            _message.value = "El nombre debe tener entre 1 y ${Sede.MAX_LARGO} caracteres"
            return
        }

        // Mismo criterio que Usuarios y Tipos: solo letras y un espacio entre palabras
        if (!Validaciones.esSoloLetras(valor)) {
            _message.value = "El nombre solo puede contener letras y un espacio entre palabras (sin números ni símbolos)"
            return
        }

        val minutos = tiempoAlerta.trim().toIntOrNull()
        if (minutos == null || minutos !in Sede.ALERTA_MIN..Sede.ALERTA_MAX) {
            _message.value = "El tiempo de alerta debe estar entre ${Sede.ALERTA_MIN} y ${Sede.ALERTA_MAX} minutos"
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val sede = Sede(id = id, nombre = valor, tiempoAlerta = minutos)
                    if (id == 0) dao.insert(sede) else dao.update(sede)
                }
                _message.value = null
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _message.value = "La sede \"$valor\" ya existe"
            }
        }
    }

    /** DELETE. Falla si la sede tiene probadores (FK RESTRICT). */
    fun delete(sede: Sede) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { dao.delete(sede) }
            } catch (e: SQLiteConstraintException) {
                _message.value = "No se puede eliminar \"${sede.nombre}\": tiene probadores"
            }
        }
    }
}