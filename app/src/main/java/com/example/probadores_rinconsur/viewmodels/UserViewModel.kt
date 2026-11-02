package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.repository.models.User
import com.example.probadores_rinconsur.repository.models.UsuarioSede
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)

    private val userDao = database.userDao()
    private val usuarioSedeDao = database.usuarioSedeDao()
    private val sedeDao = database.sedeDao()

    // READ
    val users: StateFlow<List<User>> = userDao.getAllUsers()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Mensajes de error para la vista
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    // Obtiene un usuario por su ID
    suspend fun getUser(id: Int): User? =
        withContext(Dispatchers.IO) {
            userDao.getUserById(id)
        }

    // Obtiene las sedes que tiene asignadas un usuario
    suspend fun getSedesAsignadas(
        usuarioId: Int
    ): List<Sede> =
        withContext(Dispatchers.IO) {

            val asignaciones =
                usuarioSedeDao.getAsignacionesPorUsuario(usuarioId)

            asignaciones.mapNotNull { asignacion ->
                sedeDao.getById(asignacion.sedeId)
            }
        }

    // CREATE / UPDATE
    // También guarda las sedes asignadas al usuario.
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

                withContext(Dispatchers.IO) {

                    val user = User(
                        id = id,
                        name = name,
                        email = email,
                        password = password,
                        tipoUsuarioId = tipoUsuarioId
                    )

                    val usuarioId: Int

                    if (id == 0) {

                        // CREATE
                        // Room devuelve el ID generado del nuevo usuario.
                        usuarioId =
                            userDao.insertUser(user).toInt()

                    } else {

                        // UPDATE
                        userDao.updateUser(user)
                        usuarioId = id
                    }

                    // Eliminamos las asignaciones anteriores.
                    usuarioSedeDao.deletePorUsuario(usuarioId)

                    // Guardamos las nuevas sedes seleccionadas.
                    sedesAsignadas.forEach { sedeId ->

                        usuarioSedeDao.insert(
                            UsuarioSede(
                                usuarioId = usuarioId,
                                sedeId = sedeId
                            )
                        )
                    }
                }

                _message.value = null
                onSuccess()

            } catch (e: SQLiteConstraintException) {

                _message.value =
                    "Ya existe un usuario con el email $email"
            }
        }
    }

    // DELETE
    fun deleteUser(user: User) {

        viewModelScope.launch(Dispatchers.IO) {

            userDao.deleteUser(user)
        }
    }
}