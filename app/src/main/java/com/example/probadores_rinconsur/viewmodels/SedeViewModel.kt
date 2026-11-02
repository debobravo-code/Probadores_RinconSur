package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.EstadoProbador
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SedeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sedeDao = db.sedeDao()
    private val probadorDao = db.probadorDao()

    val sedes: StateFlow<List<Sede>> = sedeDao.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    suspend fun getCantidadProbadores(sedeId: Int): Int {
        return withContext(Dispatchers.IO) {
            probadorDao
                .getProbadoresBySede(sedeId)
                .first()
                .size
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
                "El nombre solo puede contener letras y un espacio entre palabras " +
                        "(sin números ni símbolos)"
            return
        }

        val minutos = tiempoAlerta
            .trim()
            .toIntOrNull()

        if (
            minutos == null ||
            minutos !in Sede.ALERTA_MIN..Sede.ALERTA_MAX
        ) {
            _message.value =
                "El tiempo de alerta debe estar entre " +
                        "${Sede.ALERTA_MIN} y ${Sede.ALERTA_MAX} minutos"
            return
        }

        val cantidad = cantidadProbadores
            .trim()
            .toIntOrNull()

        if (cantidad == null || cantidad !in 1..20) {
            _message.value =
                "La cantidad de probadores debe estar entre 1 y 20"
            return
        }

        viewModelScope.launch {

            try {

                withContext(Dispatchers.IO) {

                    if (id == 0) {

                        // CREAR SEDE NUEVA
                        db.withTransaction {

                            val nuevaSedeId = sedeDao.insert(
                                Sede(
                                    nombre = valor,
                                    tiempoAlerta = minutos
                                )
                            ).toInt()

                            // Crear automáticamente los probadores.
                            for (numero in 1..cantidad) {

                                probadorDao.insertProbador(
                                    Probador(
                                        numero = numero,
                                        sedeId = nuevaSedeId,
                                        estado = EstadoProbador.LIBRE,
                                        inicioOcupacion = null,
                                        cantidadPrendas = 0
                                    )
                                )
                            }
                        }

                    } else {

                        // EDITAR SEDE
                        db.withTransaction {

                            val probadoresActuales =
                                probadorDao
                                    .getProbadoresBySede(id)
                                    .first()

                            val cantidadActual =
                                probadoresActuales.size

                            // Por seguridad no eliminamos
                            // probadores automáticamente.
                            if (cantidad < cantidadActual) {
                                throw IllegalArgumentException(
                                    "No se puede reducir la cantidad de probadores. " +
                                            "Actualmente esta sede tiene $cantidadActual."
                                )
                            }

                            // Actualizar nombre y alerta.
                            sedeDao.update(
                                Sede(
                                    id = id,
                                    nombre = valor,
                                    tiempoAlerta = minutos
                                )
                            )

                            // Si aumentó la cantidad,
                            // crear solamente los nuevos.
                            if (cantidad > cantidadActual) {

                                for (
                                numero in
                                (cantidadActual + 1)..cantidad
                                ) {

                                    probadorDao.insertProbador(
                                        Probador(
                                            numero = numero,
                                            sedeId = id,
                                            estado = EstadoProbador.LIBRE,
                                            inicioOcupacion = null,
                                            cantidadPrendas = 0
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                _message.value = null
                onSuccess()

            } catch (e: IllegalArgumentException) {

                _message.value = e.message

            } catch (e: SQLiteConstraintException) {

                _message.value =
                    "La sede \"$valor\" ya existe"
            }
        }
    }

    fun delete(sede: Sede) {

        viewModelScope.launch {

            try {

                withContext(Dispatchers.IO) {
                    sedeDao.delete(sede)
                }

            } catch (e: SQLiteConstraintException) {

                _message.value =
                    "No se puede eliminar \"${sede.nombre}\": tiene probadores"
            }
        }
    }
}