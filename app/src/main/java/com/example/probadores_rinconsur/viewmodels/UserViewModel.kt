package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    // READ
    val users: StateFlow<List<User>> = userDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mensajes de error para la vista (ej: email duplicado)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    suspend fun getUser(id: Int): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)
    }

    // CREATE / UPDATE. onSuccess se ejecuta en el hilo principal.
    fun saveUser(
        id: Int,
        name: String,
        email: String,
        password: String,
        imageUri: String?,
        tipoUsuarioId: Int,
        onSuccess: () -> Unit
    ) {
        // Validación: User.name solo admite letras (mayúsculas/minúsculas) y espacios entre palabras
        if (!Validaciones.esSoloLetras(name)) {
            _message.value = "El nombre solo puede contener letras y un espacio entre palabras (sin números ni símbolos)"
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
                        profileImageUri = imageUri,
                        tipoUsuarioId = tipoUsuarioId
                    )

                    if (id == 0) {
                        userDao.insertUser(user)
                    } else {
                        userDao.updateUser(user)
                    }
                }
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _message.value = "Ya existe un usuario con el email $email"
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