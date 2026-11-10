
package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
 * - Edita solamente registros autorizados.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val registroDao = db.registroDao()
    private val usuarioSedeDao = db.usuarioSedeDao()

    private val TAG = "HistorialPermisos"

    /**
     * Usuario que inició sesión.
     */
    private data class Sesion(
        val usuarioId: Int?,
        val esAdmin: Boolean
    )

    private val sesion = MutableStateFlow(
        Sesion(
            usuarioId = null,
            esAdmin = false
        )
    )

    /**
     * Registros generales.
     *
     * Se conservan por compatibilidad.
     * Las pantallas deben usar registrosPermitidos.
     */
    val registros: StateFlow<List<RegistroOcupacion>> =
        registroDao.getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    /**
     * Sedes generales.
     *
     * Se conservan por compatibilidad.
     * Las pantallas deben usar sedesPermitidas.
     */
    val sedes: StateFlow<List<Sede>> =
        db.sedeDao().getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    /**
     * Obtener las sedes según la sesión.
     *
     * Se utiliza un único flujo compartido.
     */
    val sedesPermitidas: StateFlow<List<Sede>> =
        sesion
            .flatMapLatest { usuario ->

                Log.d(
                    TAG,
                    "Consultando usuario=${usuario.usuarioId}, " +
                            "administrador=${usuario.esAdmin}"
                )

                when {

                    usuario.usuarioId == null -> {
                        flowOf(emptyList())
                    }

                    usuario.esAdmin -> {
                        db.sedeDao().getAll()
                    }

                    else -> {
                        usuarioSedeDao.getSedesPorUsuario(
                            usuario.usuarioId
                        )
                    }
                }
            }
            .onEach { lista ->

                Log.d(
                    TAG,
                    "Sedes autorizadas: " +
                            lista.map { "${it.id}: ${it.nombre}" }
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )

    /**
     * Mostrar solamente los registros
     * correspondientes a las sedes permitidas.
     */
    val registrosPermitidos: StateFlow<List<RegistroOcupacion>> =
        combine(
            sesion,
            sedesPermitidas,
            registroDao.getAll()
        ) { usuario, listaSedes, listaRegistros ->

            if (usuario.usuarioId == null) {

                emptyList()

            } else {

                val idsPermitidos =
                    listaSedes.map { it.id }.toSet()

                listaRegistros.filter {
                    it.sedeId in idsPermitidos
                }
            }
        }
            .onEach { lista ->

                Log.d(
                    TAG,
                    "Registros visibles: ${lista.size}"
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    /**
     * Configurar el usuario conectado.
     *
     * Se llama desde navegations.kt.
     */
    fun configurarUsuario(
        usuarioId: Int?,
        esAdmin: Boolean
    ) {

        Log.d(
            TAG,
            "Configurando usuario=$usuarioId, admin=$esAdmin"
        )

        sesion.value = Sesion(
            usuarioId = usuarioId,
            esAdmin = usuarioId != null && esAdmin
        )
    }

    /**
     * Actualizar un registro existente.
     *
     * Solo permite cambiar:
     * - Cantidad de prendas.
     * - Prenda olvidada.
     * - Observación.
     *
     * Conserva sede, probador y horarios.
     */
    fun actualizarRegistro(
        registro: RegistroOcupacion
    ) {

        val usuarioAlSolicitar = sesion.value

        if (usuarioAlSolicitar.usuarioId == null) {

            Log.w(TAG, "Edición rechazada: sin sesión")
            return
        }

        if (registro.cantidadPrendas < 0) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {

            if (sesion.value != usuarioAlSolicitar) {
                return@launch
            }

            // Obtener el registro original
            val original = registroDao.getAll()
                .first()
                .firstOrNull { it.id == registro.id }
                ?: return@launch

            // Consultar permisos actuales
            val autorizado =
                if (usuarioAlSolicitar.esAdmin) {

                    db.sedeDao().getAll()
                        .first()
                        .any { it.id == original.sedeId }

                } else {

                    usuarioSedeDao
                        .getAsignacionesPorUsuario(
                            usuarioAlSolicitar.usuarioId
                        )
                        .any {
                            it.sedeId == original.sedeId
                        }
                }

            if (!autorizado) {

                Log.w(
                    TAG,
                    "Edición rechazada: usuario sin permiso " +
                            "para sede ${original.sedeId}"
                )

                return@launch
            }

            // No permitir cambiar la sede
            if (original.sedeId != registro.sedeId) {
                return@launch
            }

            if (sesion.value != usuarioAlSolicitar) {
                return@launch
            }

            // Actualizar solamente campos editables
            val actualizado = original.copy(
                cantidadPrendas = registro.cantidadPrendas,
                prendaOlvidada = registro.prendaOlvidada,
                observacion = registro.observacion.trim()
            )

            registroDao.update(actualizado)

            Log.d(
                TAG,
                "Registro ${original.id} actualizado correctamente"
            )
        }
    }
}