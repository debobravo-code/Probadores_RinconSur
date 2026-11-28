package com.example.probadores_rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.TipoUsuarioRepository
import com.example.probadores_rinconsur.repository.models.TipoUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SIN_CONEXION = "No se pudo conectar con el servidor"

class TipoUsuarioViewModel(
    private val repo: TipoUsuarioRepository = TipoUsuarioRepository()
) : ViewModel() {

    // La lista viene de Room (copia local de MySQL)
    val tipos: StateFlow<List<TipoUsuario>> =
        repo.tipos.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        cargarTipos()
    }

    fun clearMessage() {
        _message.value = null
    }

    /** Vuelve a pedir los tipos de usuario al servidor. */
    fun cargarTipos() {
        viewModelScope.launch {
            actualizarListado()
        }
    }

    private suspend fun actualizarListado() {
        // Si no hay conexión, Room conserva la última copia y se sigue viendo
        if (!repo.refrescar()) {
            _message.value =
                "No se pudo actualizar desde el servidor. Se muestran los datos guardados"
        }
    }

    /** CREATE (id = 0) o UPDATE (id > 0). Devuelve el resultado por onSuccess. */
    fun save(id: Int, nombre: String, onSuccess: () -> Unit = {}) {
        val valor = nombre.trim()
        if (valor.isEmpty() || valor.length > TipoUsuario.MAX_LARGO) {
            _message.value = "El tipo debe tener entre 1 y ${TipoUsuario.MAX_LARGO} caracteres"
            return
        }

        // Solo letras (mayúsculas/minúsculas) y un espacio entre palabras
        if (!Validaciones.esSoloLetras(valor)) {
            _message.value = "El tipo solo puede contener letras y un espacio entre palabras (sin números ni símbolos)"
            return
        }

        viewModelScope.launch {
            try {
                val tipo = TipoUsuario(id = id, tipoUsuario = valor)

                val respuesta = if (id == 0) {
                    repo.crear(tipo)
                } else {
                    repo.actualizar(id, tipo)
                }

                if (respuesta.isSuccessful) {
                    _message.value = null
                    onSuccess()
                } else {
                    _message.value = when (respuesta.code()) {
                        403 -> "El tipo Administrador no se puede modificar"
                        404 -> "Tipo de usuario no encontrado"
                        409 -> "El tipo \"$valor\" ya existe"
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

    /** DELETE. El servidor lo rechaza si algún usuario usa el tipo. */
    fun delete(tipo: TipoUsuario) {
        viewModelScope.launch {
            try {
                val respuesta = repo.eliminar(tipo.id)

                if (respuesta.isSuccessful) {
                    _message.value = null
                } else {
                    _message.value = when (respuesta.code()) {
                        403 -> "El tipo Administrador no se puede eliminar"
                        404 -> "Tipo de usuario no encontrado"
                        409 -> "No se puede eliminar \"${tipo.tipoUsuario}\": hay usuarios con ese tipo"
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
