package com.example.probadores_rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.remote.SedeRepository
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.Retrofit.dto.SedeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Sedes. La lista sale de Room (copia local) y el repositorio la mantiene
 * igual a MySQL: cada cambio se guarda en el microservicio y luego se actualiza Room.
 */
class SedeViewModel(
    private val repo: SedeRepository = SedeRepository()
) : ViewModel() {

    // La pantalla observa Room; cuando Room cambia, la pantalla se actualiza sola
    val sedes: StateFlow<List<Sede>> =
        repo.sedes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        cargarSedes()
    }

    fun clearMessage() {
        _message.value = null
    }

    fun cargarSedes() {
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

    suspend fun getCantidadProbadores(sedeId: Int): Int {
        return try {
            val response = repo.probadoresDeSede(sedeId)

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.size
            } else {
                _message.value = "No se pudieron consultar los probadores"
                -1
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _message.value = "No se pudo conectar con el servidor"
            -1
        }
    }

    fun save(
        id: Int,
        nombre: String,
        tiempoAlerta: String,
        cantidadProbadores: String,
        onSuccess: () -> Unit = {}
    ) {
        val valor = nombre.trim()

        if (valor.isEmpty() || valor.length > Sede.MAX_LARGO) {
            _message.value =
                "El nombre debe tener entre 1 y ${Sede.MAX_LARGO} caracteres"
            return
        }

        if (!Validaciones.esSoloLetras(valor)) {
            _message.value =
                "El nombre solo puede contener letras y un espacio entre palabras"
            return
        }

        val minutos = tiempoAlerta.trim().toIntOrNull()

        if (minutos == null ||
            minutos !in Sede.ALERTA_MIN..Sede.ALERTA_MAX
        ) {
            _message.value =
                "El tiempo de alerta debe estar entre " +
                "${Sede.ALERTA_MIN} y ${Sede.ALERTA_MAX} minutos"
            return
        }

        val cantidad = cantidadProbadores.trim().toIntOrNull()

        if (cantidad == null || cantidad !in 1..20) {
            _message.value =
                "La cantidad de probadores debe estar entre 1 y 20"
            return
        }

        viewModelScope.launch {
            try {

                if (id != 0) {
                    val cantidadActual = getCantidadProbadores(id)

                    if (cantidadActual == -1) {
                        return@launch
                    }

                    if (cantidad < cantidadActual) {
                        _message.value =
                            "No se puede reducir la cantidad de probadores"
                        return@launch
                    }
                }

                val request = SedeRequest(
                    nombre = valor,
                    tiempoAlerta = minutos,
                    cantidadProbadores = cantidad
                )

                val response = if (id == 0) {
                    repo.crear(request)
                } else {
                    repo.actualizar(id, request)
                }

                if (response.isSuccessful) {
                    _message.value = null
                    onSuccess()
                } else {
                    _message.value = when (response.code()) {
                        400 -> "Los datos ingresados no son validos"
                        404 -> "Sede no encontrada"
                        409 -> "La sede ya existe o la operacion genera un conflicto"
                        else -> "Error del servidor: ${response.code()}"
                    }
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _message.value = "No se pudo conectar con el servidor"
            }
        }
    }

    fun delete(sede: Sede) {
        viewModelScope.launch {
            try {
                val response = repo.eliminar(sede.id)

                if (response.isSuccessful) {
                    _message.value = null
                } else {
                    _message.value = when (response.code()) {
                        404 -> "Sede no encontrada"
                        409 -> "No se puede eliminar la sede: tiene registros asociados"
                        else -> "Error del servidor: ${response.code()}"
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _message.value = "No se pudo conectar con el servidor"
            }
        }
    }
}