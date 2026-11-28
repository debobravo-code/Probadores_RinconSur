package com.example.probadores_rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.UserRepository
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.repository.models.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SIN_CONEXION = "No se pudo conectar con el servidor"

class UserViewModel(
    private val repo: UserRepository = UserRepository()
) : ViewModel() {

    // READ: la lista viene de Room (copia local de MySQL)
    val users: StateFlow<List<User>> =
        repo.users.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Mensajes de error para la vista
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        cargarUsuarios()
    }

    fun clearMessage() {
        _message.value = null
    }

    /** Vuelve a pedir la lista de usuarios al servidor. */
    fun cargarUsuarios() {
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

    // Obtiene un usuario por su ID
    suspend fun getUser(id: Int): User? {
        return repo.obtener(id)
    }

    // Obtiene las sedes que tiene asignadas un usuario
    suspend fun getSedesAsignadas(usuarioId: Int): List<Sede> {
        // Primero se actualiza Room desde MySQL; si no hay conexión se usa la última copia
        repo.refrescarSedesDeUsuario(usuarioId)
        return repo.sedesDeUsuario(usuarioId).first()
    }

    // CREATE / UPDATE
    // También guarda las sedes asignadas al usuario.
    // Al editar, una contraseña vacía significa "no cambiarla".
    fun saveUser(
        id: Int,
        name: String,
        email: String,
        password: String,
        tipoUsuarioId: Int,
        sedesAsignadas: Set<Int>,
        onSuccess: () -> Unit
    ) {

        // Validación del nombre
        if (!Validaciones.esSoloLetras(name)) {
            _message.value =
                "El nombre solo puede contener letras y un espacio entre palabras " +
                        "(sin números ni símbolos)"
            return
        }

        viewModelScope.launch {
            try {
                val user = User(
                    id = id,
                    name = name,
                    email = email,
                    password = password,
                    tipoUsuarioId = tipoUsuarioId
                )

                val respuesta = if (id == 0) {
                    repo.crear(user)
                } else {
                    repo.actualizar(id, user)
                }

                if (!respuesta.isSuccessful) {
                    _message.value = when (respuesta.code()) {
                        400 -> "Los datos ingresados no son válidos"
                        404 -> "Usuario no encontrado"
                        409 -> "Ya existe un usuario con el email $email"
                        else -> "Error del servidor: ${respuesta.code()}"
                    }
                    return@launch
                }

                // Al crear, el servidor devuelve el id del nuevo usuario
                val usuarioId = respuesta.body()?.id ?: id

                val asignacion = repo.asignarSedes(usuarioId, sedesAsignadas.toList())

                if (!asignacion.isSuccessful) {
                    // Si el usuario era nuevo se elimina, para poder intentarlo otra vez
                    if (id == 0) {
                        repo.eliminar(usuarioId)
                    }
                    _message.value = "No se pudieron asignar las sedes. Intente nuevamente"
                    return@launch
                }

                _message.value = null
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _message.value = SIN_CONEXION
            }
        }
    }

    // DELETE
    fun deleteUser(user: User) {
        viewModelScope.launch {
            try {
                val respuesta = repo.eliminar(user.id)

                if (respuesta.isSuccessful) {
                    _message.value = null
                } else {
                    _message.value = when (respuesta.code()) {
                        404 -> "Usuario no encontrado"
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
