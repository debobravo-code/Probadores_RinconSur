package com.example.probadores_rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.models.EstadoProbador
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.remote.ProbadorRepository
import com.example.probadores_rinconsur.repository.remote.SedeRepository
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Response

private const val SIN_CONEXION = "No se pudo conectar con el servidor"
private const val SIN_ACTUALIZAR =
    "No se pudo actualizar desde el servidor. Se muestran los datos guardados"

/**
 * Probadores y sedes del panel. Los datos viven en MySQL (microservicio) y los
 * repositorios mantienen una copia en Room, que es lo que lee la pantalla.
 */
class ProbadorViewModel(
    private val probadorRepo: ProbadorRepository = ProbadorRepository(),
    private val sedeRepo: SedeRepository = SedeRepository()
) : ViewModel() {

    // READ: listas expuestas a la vista (vienen de Room)
    val probadores: StateFlow<List<Probador>> =
        probadorRepo.probadores.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val sedes: StateFlow<List<Sede>> =
        sedeRepo.sedes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Mensajes para la vista (avisos de la operación o errores)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        cargarDatos()
    }

    fun clearMessage() {
        _message.value = null
    }

    /** Vuelve a pedir probadores y sedes al servidor. */
    fun cargarDatos() {
        viewModelScope.launch {
            actualizarListados()
        }
    }

    private suspend fun actualizarListados() {
        // Si no hay conexión, Room conserva la última copia y se sigue viendo
        val probadoresOk = probadorRepo.refrescar()
        val sedesOk = sedeRepo.refrescar()

        if (!probadoresOk || !sedesOk) {
            _message.value = SIN_ACTUALIZAR
        }
    }

    // Lectura puntual (se invoca desde una corrutina de la vista: LaunchedEffect)
    suspend fun getProbador(id: Int): Probador? {
        return probadorRepo.obtener(id)
    }

    private fun mensajeError(codigo: Int): String = when (codigo) {
        400 -> "Los datos ingresados no son válidos"
        404 -> "Probador no encontrado"
        409 -> "La operación no es válida para este probador (número repetido o estado distinto)"
        else -> "Error del servidor: $codigo"
    }

    // ---------------- CRUD (solo Administrador) ----------------

    /** CREATE (id = 0) / UPDATE. El switch "habilitado" solo cambia un probador libre o fuera de servicio. */
    fun saveProbador(id: Int, numero: Int, sedeId: Int, habilitado: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // El número no se puede repetir dentro de la misma sede
                val repetido = probadores.value.any {
                    it.sedeId == sedeId && it.numero == numero && it.id != id
                }
                if (repetido) {
                    _message.value = "Ya existe el probador $numero en esa sede"
                    return@launch
                }

                // Un probador ocupado o con prenda olvidada no se puede modificar
                if (id != 0) {
                    val actual = probadorRepo.obtener(id)
                    if (actual != null &&
                        (actual.estado == EstadoProbador.OCUPADO ||
                                actual.estado == EstadoProbador.PRENDA_OLVIDADA)
                    ) {
                        _message.value =
                            "No se puede modificar un probador ocupado o con prenda olvidada"
                        return@launch
                    }
                }

                val probador = Probador(id = id, numero = numero, sedeId = sedeId)
                val guardado = if (id == 0) {
                    probadorRepo.crear(probador)
                } else {
                    probadorRepo.actualizar(id, probador)
                }

                if (!guardado.isSuccessful) {
                    _message.value = mensajeError(guardado.code())
                    return@launch
                }

                // "Habilitado" = LIBRE, "no habilitado" = FUERA_DE_SERVICIO
                val estadoDeseado =
                    if (habilitado) EstadoProbador.LIBRE else EstadoProbador.FUERA_DE_SERVICIO
                val nuevo = guardado.body()

                if (nuevo != null && nuevo.estado != estadoDeseado) {
                    val cambio = probadorRepo.cambiarEstado(nuevo.id, estadoDeseado)
                    if (!cambio.isSuccessful) {
                        _message.value = mensajeError(cambio.code())
                        return@launch
                    }
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

    /** DELETE. El servidor lo rechaza si el probador tiene reportes o no está libre. */
    fun deleteProbador(probador: Probador) {
        ejecutar(
            exito = "Probador ${probador.numero} eliminado",
            error = "No se puede eliminar el probador ${probador.numero}: tiene reportes o no está libre"
        ) {
            probadorRepo.eliminar(probador.id)
        }
    }

    // ---------------- Operación diaria (Personal y Administrador) ----------------

    /** Solo un probador LIBRE se puede ocupar. El servidor guarda la hora de inicio y la cantidad de prendas (1 a 4). */
    fun ocupar(p: Probador, cantidadPrendas: Int) {
        if (cantidadPrendas !in 1..Probador.MAX_PRENDAS) {
            _message.value = "La cantidad de prendas debe estar entre 1 y ${Probador.MAX_PRENDAS}"
            return
        }
        ejecutar(
            exito = "Probador ${p.numero} ocupado con $cantidadPrendas prenda(s)",
            error = "Ese probador no está libre"
        ) {
            probadorRepo.ocupar(p.id, cantidadPrendas)
        }
    }

    /** Al finalizar: vuelve a LIBRE o pasa a PRENDA_OLVIDADA. El servidor guarda el uso en el historial. */
    fun finalizar(p: Probador, quedoPrenda: Boolean) {
        ejecutar(
            exito = if (quedoPrenda) "Probador ${p.numero}: prenda olvidada" else "Probador ${p.numero} libre",
            error = "El probador ${p.numero} ya no está ocupado"
        ) {
            if (quedoPrenda) {
                probadorRepo.cambiarEstado(p.id, EstadoProbador.PRENDA_OLVIDADA)
            } else {
                probadorRepo.liberar(p.id)
            }
        }
    }

    /** La incidencia se solucionó: PRENDA_OLVIDADA vuelve a LIBRE. */
    fun solucionarPrenda(p: Probador) {
        ejecutar(
            exito = "Probador ${p.numero} libre",
            error = "El probador ${p.numero} no tiene una prenda olvidada"
        ) {
            probadorRepo.cambiarEstado(p.id, EstadoProbador.LIBRE)
        }
    }

    /**
     * Ejecuta una llamada al servidor y avisa el resultado.
     * Si salió bien, el repositorio ya actualizó Room; si falló, se vuelve a pedir
     * la lista para mostrar el estado real.
     */
    private fun ejecutar(
        exito: String,
        error: String,
        llamada: suspend () -> Response<*>
    ) {
        viewModelScope.launch {
            try {
                val respuesta = llamada()

                _message.value = when {
                    respuesta.isSuccessful -> exito
                    respuesta.code() == 409 -> error
                    else -> mensajeError(respuesta.code())
                }

                if (!respuesta.isSuccessful) {
                    probadorRepo.refrescar()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _message.value = SIN_CONEXION
            }
        }
    }
}
