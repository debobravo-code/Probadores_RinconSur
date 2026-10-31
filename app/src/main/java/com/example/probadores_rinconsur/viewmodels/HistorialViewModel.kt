package com.example.probadores_rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.data.AppDatabase
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.Sede
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Solo lectura: los registros los crea ProbadorViewModel al finalizar un uso. */
class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    val registros: StateFlow<List<RegistroOcupacion>> = db.registroDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sedes: StateFlow<List<Sede>> = db.sedeDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}