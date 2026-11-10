
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
/**
 * Gestiona el historial y los permisos por sede.
 *
 * Administrador: todas las sedes.
 * Personal: solamente sus sedes asignadas.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HistorialViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val registroDao = db.registroDao()
    private val usuarioSedeDao = db.usuarioSedeDao()

    /**
     * Información del usuario conectado.
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
     * Listas generales de Room.
     *
     * Se conservan para compatibilidad.
     * Las pantallas deben utilizar las
     * listas filtradas por permisos.
     */
    val registros: StateFlow<List<RegistroOcupacion>> =
        registroDao.getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val sedes: StateFlow<List<Sede>> =
        db.sedeDao().getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    /**
     * Obtiene las sedes autorizadas
     * según el usuario y su rol.
     */
    private val sedesAutorizadas =
        sesion.flatMapLatest { usuario ->

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

    /**
     * Sedes que puede ver el usuario.
     */
    val sedesPermitidas: StateFlow<List<Sede>> =
        sedesAutorizadas
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    /**
     * Historial filtrado por sedes autorizadas.
     *
     * Si el usuario no tiene sedes,
     * no se muestran registros.
     */
    val registrosPermitidos: StateFlow<List<RegistroOcupacion>> =
        combine(
            sesion,
            sedesAutorizadas,
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
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    /**
     * Configura al usuario autenticado.
     *
     * Se llamará desde navegations.kt.
     */
    fun configurarUsuario(
        usuarioId: Int?,
        esAdmin: Boolean
    ) {

        sesion.value = Sesion(
            usuarioId = usuarioId,
            esAdmin = usuarioId != null && esAdmin
        )
    }

    /**
     * Actualiza un registro existente.
     *
     * Comprueba los permisos antes de guardar.
     * Solo permite modificar:
     * - Cantidad de prendas
     * - Prenda olvidada
     * - Observación
     *
     * No cambia sede, probador ni horarios.
     */
    fun actualizarRegistro(
        registro: RegistroOcupacion
    ) {

        val usuarioAlSolicitar = sesion.value

        if (usuarioAlSolicitar.usuarioId == null) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {

            // Evitar guardar si cambió la sesión
            if (sesion.value != usuarioAlSolicitar) {
                return@launch
            }

            val autorizado = if (usuarioAlSolicitar.esAdmin) {

                // El administrador puede editar
                // registros de sedes existentes.
                val sedesActuales =
                    db.sedeDao().getAll()
                        .first()

                sedesActuales.any {
                    it.id == registro.sedeId
                }

            } else {

                // El personal necesita tener
                // la sede asignada.
                val asignaciones =
                    usuarioSedeDao.getAsignacionesPorUsuario(
                        usuarioAlSolicitar.usuarioId
                    )

                asignaciones.any {
                    it.sedeId == registro.sedeId
                }
            }

            if (!autorizado) {
                return@launch
            }

            // Consultar el registro original.
            val original = registroDao.getAll().first()
                .firstOrNull { it.id == registro.id }
                ?: return@launch

            // Impedir modificar otra sede
            if (original.sedeId != registro.sedeId) {
                return@launch
            }

            // Comprobar nuevamente la sesión
            if (sesion.value != usuarioAlSolicitar) {
                return@launch
            }

            // Conservar los datos originales
            val actualizado = original.copy(
                cantidadPrendas = registro.cantidadPrendas,
                prendaOlvidada = registro.prendaOlvidada,
                observacion = registro.observacion
            )

            registroDao.update(actualizado)
        }
    }
}
