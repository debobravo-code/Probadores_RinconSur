package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.models.User
import com.example.probadores_rinconsur.repository.remote.RetrofitClient
import com.example.probadores_rinconsur.repository.remote.dto.LoginRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedUser: User? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, error = null) }

    fun login() {
        val state = _uiState.value

        if (state.isLoading) return

        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update {
                it.copy(error = "Ingrese email y contraseña")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            try {
                val response = apiService.login(
                    LoginRequest(
                        email = state.email.trim(),
                        password = state.password
                    )
                )

                val usuario = response.body()

                if (response.isSuccessful && usuario != null) {

                    val user = User(
                        id = usuario.id,
                        name = usuario.name,
                        email = usuario.email,
                        password = "",
                        tipoUsuarioId = usuario.tipoUsuarioId
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loggedUser = user,
                            password = ""
                        )
                    }

                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = if (response.code() == 401)
                                "Credenciales inválidas"
                            else
                                "Error del servidor: ${response.code()}"
                        )
                    }
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No se pudo conectar con el servidor"
                    )
                }
            }
        }
    }

    fun logout() {
        _uiState.value = LoginUiState()
    }
}