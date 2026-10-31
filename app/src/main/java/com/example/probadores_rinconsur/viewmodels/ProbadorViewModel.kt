package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.EstadoProbador
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProbadorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val probadorDao = db.probadorDao()
    private val registroDao = db.registroDao()

    // READ: listas reactivas expuestas a la vista
    val probadores: StateFlow<List<Probador>> = probadorDao.getAllProbadores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sedes: StateFlow<List<Sede>> = db.sedeDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mensajes para la vista (avisos de la operación o errores)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    // Lectura puntual (se invoca desde una corrutina de la vista: LaunchedEffect)
    suspend fun getProbador(id: Int): Probador? = withContext(Dispatchers.IO) {
        probadorDao.getProbadorById(id)
    }

    // ---------------- CRUD (solo Administrador) ----------------

    /** CREATE (id = 0) / UPDATE. El switch "habilitado" solo cambia un probador libre o fuera de servicio. */
    fun saveProbador(id: Int, numero: Int, sedeId: Int, habilitado: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val actual = if (id == 0) null else probadorDao.getProbadorById(id)
                    val estado = when {
                        actual != null && actual.estado != EstadoProbador.LIBRE &&
                                actual.estado != EstadoProbador.FUERA_DE_SERVICIO -> actual.estado
                        habilitado -> EstadoProbador.LIBRE
                        else -> EstadoProbador.FUERA_DE_SERVICIO
                    }
                    val probador = Probador(
                        id = id, numero = numero, sedeId = sedeId,
                        estado = estado, inicioOcupacion = actual?.inicioOcupacion,
                        cantidadPrendas = actual?.cantidadPrendas ?: 0
                    )
                    if (id == 0) probadorDao.insertProbador(probador) else probadorDao.updateProbador(probador)
                }
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _message.value = "Ya existe el probador $numero en esa sede"
            }
        }
    }

    /** DELETE. Falla si el probador tiene reportes (FK RESTRICT). */
    fun deleteProbador(probador: Probador) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { probadorDao.deleteProbador(probador) }
            } catch (e: SQLiteConstraintException) {
                _message.value = "No se puede eliminar el probador ${probador.numero}: tiene reportes"
            }
        }
    }

    // ---------------- Operación diaria (Personal y Administrador) ----------------

    /** Solo un probador LIBRE se puede ocupar. Guarda la hora de inicio y la cantidad de prendas (1 a 4). */
    fun ocupar(p: Probador, cantidadPrendas: Int) {
        if (cantidadPrendas !in 1..Probador.MAX_PRENDAS) {
            _message.value = "La cantidad de prendas debe estar entre 1 y ${Probador.MAX_PRENDAS}"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val actual = probadorDao.getProbadorById(p.id) ?: return@launch
            if (actual.estado != EstadoProbador.LIBRE) {
                _message.value = "Ese probador no está libre"
                return@launch
            }
            probadorDao.updateProbador(
                actual.copy(
                    estado = EstadoProbador.OCUPADO,
                    inicioOcupacion = System.currentTimeMillis(),
                    cantidadPrendas = cantidadPrendas
                )
            )
            _message.value = "Probador ${p.numero} ocupado con $cantidadPrendas prenda(s)"
        }
    }

    /** Al finalizar: vuelve a LIBRE o pasa a PRENDA_OLVIDADA. Guarda el uso en el historial (transacción). */
    fun finalizar(p: Probador, quedoPrenda: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            db.withTransaction {
                val actual = probadorDao.getProbadorById(p.id) ?: return@withTransaction
                val inicio = actual.inicioOcupacion
                if (actual.estado != EstadoProbador.OCUPADO || inicio == null) return@withTransaction
                registroDao.insert(
                    RegistroOcupacion(
                        sedeId = actual.sedeId, numeroProbador = actual.numero,
                        inicioMillis = inicio, finMillis = System.currentTimeMillis(),
                        prendaOlvidada = quedoPrenda, cantidadPrendas = actual.cantidadPrendas
                    )
                )
                probadorDao.updateProbador(
                    actual.copy(
                        estado = if (quedoPrenda) EstadoProbador.PRENDA_OLVIDADA else EstadoProbador.LIBRE,
                        inicioOcupacion = null,
                        cantidadPrendas = 0
                    )
                )
            }
            _message.value = if (quedoPrenda) "Probador ${p.numero}: prenda olvidada" else "Probador ${p.numero} libre"
        }
    }

    /** La incidencia se solucionó: PRENDA_OLVIDADA vuelve a LIBRE. */
    fun solucionarPrenda(p: Probador) {
        viewModelScope.launch(Dispatchers.IO) {
            val actual = probadorDao.getProbadorById(p.id) ?: return@launch
            if (actual.estado == EstadoProbador.PRENDA_OLVIDADA) {
                probadorDao.updateProbador(actual.copy(estado = EstadoProbador.LIBRE))
                _message.value = "Probador ${p.numero} libre"
            }
        }
    }
}