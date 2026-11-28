package com.example.probadores_rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.RegistroRepository
import com.example.probadores_rinconsur.repository.SedeRepository
import com.example.probadores_rinconsur.repository.UserRepository
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.repository.remote.dto.RegistroEdicionRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Gestiona el historial y sus permisos.
 *
 * Administrador:
 * - Consulta todas las sedes.
 * - Consulta y edita todos los registros.
 *
 * Personal:
 * - Consulta solamente sus sedes asignadas.
 * - Edita solamente registros de sus sedes.
 */
class HistorialViewModel(
    private val registroRepo: RegistroRepository = RegistroRepository(),
    private val sedeRepo: SedeRepository = SedeRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    // Usuario que inició sesión (se configura desde navegations.kt)
    private var usuarioId: Int? = null
    private var esAdmin: Boolean = false

    private val _sedesPermitidas = MutableStateFlow<List<Sede>>(emptyList())

    /** Sedes que el usuario puede consultar. */
    val sedesPermitidas: StateFlow<List<Sede>> = _sedesPermitidas.asStateFlow()

    private val _registrosPermitidos = MutableStateFlow<List<RegistroOcupacion>>(emptyList())

    /** Registros que pertenecen a las sedes permitidas. Las pantallas usan este flujo. */
    val registrosPermitidos: StateFlow<List<RegistroOcupacion>> = _registrosPermitidos.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    /**
     * Configurar el usuario conectado.
     * Se llama desde navegations.kt al iniciar o cerrar sesión.
     */
    fun configurarUsuario(usuarioId: Int?, esAdmin: Boolean) {
        this.usuarioId = usuarioId
        this.esAdmin = usuarioId != null && esAdmin
        cargarDatos()
    }

    /** Actualiza Room desde el servidor y luego lee de Room según el usuario conectado. */
    fun cargarDatos() {
        viewModelScope.launch {
            actualizarListados()
        }
    }

    private suspend fun actualizarListados() {
        val usuarioAlSolicitar = usuarioId

        // Sin sesión no se muestra nada
        if (usuarioAlSolicitar == null) {
            _sedesPermitidas.value = emptyList()
            _registrosPermitidos.value = emptyList()
            return
        }

        // Primero se actualiza la copia local (Room) con lo que hay en MySQL
        val sedesOk = sedeRepo.refrescar()
        val registrosOk = registroRepo.refrescar()
        val asignadasOk = esAdmin || userRepo.refrescarSedesDeUsuario(usuarioAlSolicitar)

        if (!sedesOk || !registrosOk || !asignadasOk) {
            _message.value =
                "No se pudo actualizar desde el servidor. Se muestran los datos guardados"
        }

        // Después se lee siempre desde Room (funciona también sin conexión)
        val sedes = if (esAdmin) {
            sedeRepo.sedes.first()
        } else {
            userRepo.sedesDeUsuario(usuarioAlSolicitar).first()
        }
        val registros = registroRepo.registros.first()

        // Si cambió el usuario mientras se esperaba la respuesta, se descarta
        if (usuarioAlSolicitar != usuarioId) return

        val idsPermitidos = sedes.map { it.id }.toSet()

        _sedesPermitidas.value = sedes.sortedBy { it.id }
        _registrosPermitidos.value = registros.filter { it.sedeId in idsPermitidos }
    }

    /**
     * Actualizar un registro existente.
     *
     * Solo permite cambiar cantidad de prendas, prenda olvidada y observación.
     * Conserva sede, probador y horarios.
     */
    fun actualizarRegistro(registro: RegistroOcupacion) {
        // Sin sesión no se puede editar
        if (usuarioId == null) return

        if (registro.cantidadPrendas < 0) return

        // El registro original debe estar entre los registros permitidos
        val original = _registrosPermitidos.value
            .firstOrNull { it.id == registro.id } ?: return

        // No se permite cambiar la sede
        if (original.sedeId != registro.sedeId) return

        // El usuario debe tener permiso sobre la sede del registro
        if (_sedesPermitidas.value.none { it.id == original.sedeId }) return

        viewModelScope.launch {
            try {
                val respuesta = registroRepo.actualizar(
                    registro.id,
                    RegistroEdicionRequest(
                        cantidadPrendas = registro.cantidadPrendas,
                        prendaOlvidada = registro.prendaOlvidada,
                        observacion = registro.observacion.trim()
                    )
                )

                if (respuesta.isSuccessful) {
                    _message.value = null
                    // El repositorio ya actualizó Room: se vuelve a leer la lista
                    actualizarListados()
                } else {
                    _message.value = when (respuesta.code()) {
                        400 -> "Los datos del registro no son válidos"
                        404 -> "Registro no encontrado"
                        else -> "Error del servidor: ${respuesta.code()}"
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
