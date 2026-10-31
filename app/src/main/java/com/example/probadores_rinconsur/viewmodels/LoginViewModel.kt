package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedUser: User? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, error = null) }

    fun login() {
        val state = _uiState.value

        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Ingrese email y contraseña") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            delay(600)

            val user = withContext(Dispatchers.IO) {
                userDao.login(state.email.trim(), state.password)
            }

            _uiState.update {
                if (user != null) {
                    it.copy(
                        isLoading = false,
                        loggedUser = user,
                        password = ""
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        error = "Credenciales inválidas"
                    )
                }
            }
        }
    }

    fun logout() {
        _uiState.value = LoginUiState()
    }
}