package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.TipoUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TipoUsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).tipoUsuarioDao()

    val tipos: StateFlow<List<TipoUsuario>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    /** CREATE (id = 0) o UPDATE (id > 0). Devuelve el resultado por onSuccess. */
    fun save(id: Int, nombre: String, onSuccess: () -> Unit = {}) {
        val valor = nombre.trim()
        if (valor.isEmpty() || valor.length > TipoUsuario.MAX_LARGO) {
            _message.value = "El tipo debe tener entre 1 y ${TipoUsuario.MAX_LARGO} caracteres"
            return
        }

        // Validación: TipoUsuario.tipoUsuario solo admite letras (mayúsculas/minúsculas) y espacios entre palabras
        if (!Validaciones.esSoloLetras(valor)) {
            _message.value = "El tipo solo puede contener letras y un espacio entre palabras (sin números ni símbolos)"
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val tipo = TipoUsuario(id = id, tipoUsuario = valor)
                    if (id == 0) dao.insert(tipo) else dao.update(tipo)
                }
                _message.value = null
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _message.value = "El tipo \"$valor\" ya existe"
            }
        }
    }

    /** DELETE. Falla si algún usuario usa el tipo (FK RESTRICT). */
    fun delete(tipo: TipoUsuario) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { dao.delete(tipo) }
            } catch (e: SQLiteConstraintException) {
                _message.value = "No se puede eliminar \"${tipo.tipoUsuario}\": hay usuarios con ese tipo"
            }
        }
    }
}