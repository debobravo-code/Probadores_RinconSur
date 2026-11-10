
package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel del historial de ocupación.
 *
 * Administrador:
 * - Puede consultar todas las sedes.
 * - Puede editar registros de todas las sedes.
 *
 * Personal:
 * - Solo puede consultar las sedes asignadas.
 * - Solo puede editar registros de esas sedes.
 */
class HistorialViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val registroDao = db.registroDao()
    private val usuarioSedeDao = db.usuarioSedeDao()

    // Todos los registros almacenados en Room.
    // Se conservan para compatibilidad con la pantalla.
    val registros: StateFlow<List<RegistroOcupacion>> =
        registroDao.getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // Todas las sedes registradas.
    val sedes: StateFlow<List<Sede>> =
        db.sedeDao().getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // Identificación del usuario conectado
    private val _usuarioActualId = MutableStateFlow<Int?>(null)

    private val _esAdministrador = MutableStateFlow(false)

    // Sedes autorizadas para el usuario actual
    private val _sedesPermitidas = MutableStateFlow<List<Sede>>(emptyList())

    val sedesPermitidas: StateFlow<List<Sede>> =
        _sedesPermitidas.asStateFlow()

    // Registros visibles para el usuario actual
    private val _registrosPermitidos =
        MutableStateFlow<List<RegistroOcupacion>>(emptyList())

    val registrosPermitidos: StateFlow<List<RegistroOcupacion>> =
        _registrosPermitidos.asStateFlow()

    /**
     * Configura el usuario que inició sesión.
     *
     * Se llamará desde la navegación.
     */
    fun configurarUsuario(
        usuarioId: Int?,
        esAdmin: Boolean
    ) {

        _usuarioActualId.value = usuarioId
        _esAdministrador.value = esAdmin

        // Limpiar datos de la sesión anterior
        _sedesPermitidas.value = emptyList()
        _registrosPermitidos.value = emptyList()
    }

    /**
     * Observa los datos y aplica los permisos.
     *
     * Las asignaciones se actualizan cuando
     * el administrador modifica las sedes.
     */
    init {

        viewModelScope.launch {

            _usuarioActualId.collectLatest { usuarioId ->

                if (usuarioId == null) {
                    _sedesPermitidas.value = emptyList()
                    _registrosPermitidos.value = emptyList()
                    return@collectLatest
                }

                if (_esAdministrador.value) {

                    sedes.collectLatest { listaSedes ->
                        _sedesPermitidas.value = listaSedes
                    }

                } else {

                    usuarioSedeDao
                        .getSedesPorUsuario(usuarioId)
                        .collectLatest { listaSedes ->
                            _sedesPermitidas.value = listaSedes
                        }
                }
            }
        }

        viewModelScope.launch {

            registros.collectLatest { listaRegistros ->

                val idsPermitidos =
                    _sedesPermitidas.value.map { it.id }.toSet()

                _registrosPermitidos.value =
                    listaRegistros.filter {
                        it.sedeId in idsPermitidos
                    }
            }
        }

        viewModelScope.launch {

            _sedesPermitidas.collectLatest { listaSedes ->

                val idsPermitidos =
                    listaSedes.map { it.id }.toSet()

                _registrosPermitidos.value =
                    registros.value.filter {
                        it.sedeId in idsPermitidos
                    }
            }
        }
    }

    /**
     * Actualiza un registro existente.
     *
     * Antes de guardar, comprueba que el usuario
     * tenga permiso para modificar su sede.
     */
    fun actualizarRegistro(
        registro: RegistroOcupacion
    ) {

        val usuarioId = _usuarioActualId.value
            ?: return

        viewModelScope.launch(Dispatchers.IO) {

            // Verificar las sedes autorizadas
            val idsPermitidos =
                if (_esAdministrador.value) {

                    db.sedeDao().getAll()
                        .stateIn(
                            this,
                            SharingStarted.Eagerly,
                            emptyList()
                        )
                        .value
                        .map { it.id }
                        .toSet()

                } else {

                    usuarioSedeDao
                        .getAsignacionesPorUsuario(usuarioId)
                        .map { it.sedeId }
                        .toSet()
                }

            // Solo guardar si tiene permiso
            if (registro.sedeId in idsPermitidos) {

                registroDao.update(registro)
            }
        }
    }
}
