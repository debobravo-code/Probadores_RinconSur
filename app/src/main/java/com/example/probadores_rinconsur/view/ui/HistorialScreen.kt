package com.example.probadores_rinconsur.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.probadores_rinconsur.view.theme.ColoresOlvidada
import com.example.probadores_rinconsur.view.utils.Formato
import com.example.probadores_rinconsur.view.utils.SedeChips
import com.example.probadores_rinconsur.viewmodels.HistorialViewModel

/** Usos finalizados de los probadores, filtrados por sede (0 = todas). Solo lectura. */
@Composable
fun HistorialScreen(viewModel: HistorialViewModel) {
    val registros by viewModel.registros.collectAsStateWithLifecycle()
    val sedes by viewModel.sedes.collectAsStateWithLifecycle()
    val nombreSede = sedes.associate { it.id to it.nombre }
    var sedeId by rememberSaveable { mutableIntStateOf(0) }

    val lista = if (sedeId == 0) registros else registros.filter { it.sedeId == sedeId }

    Column(modifier = Modifier.fillMaxSize()) {
        SedeChips(
            sedes,
            sedeId,
            { sedeId = it },
            Modifier.padding(top = 8.dp),
            incluirTodas = true
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (lista.isEmpty()) {
                Text(
                    "Sin registros.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(lista, key = { it.id }) { r ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "Probador ${r.numeroProbador} · ${nombreSede[r.sedeId] ?: "Sede eliminada"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        Formato.fecha(r.inicioMillis),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                Text(
                                    "${Formato.hora(r.inicioMillis)} - ${Formato.hora(r.finMillis)} · " +
                                            "${Formato.minutosEntre(r.inicioMillis, r.finMillis)} min · " +
                                            "${r.cantidadPrendas} prenda(s)"
                                )

                                if (r.prendaOlvidada) {
                                    Text(
                                        "Prenda olvidada",
                                        fontWeight = FontWeight.SemiBold,
                                        color = ColoresOlvidada.principal
                                    )
                                } else {
                                    Text(
                                        "Sin incidencia",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}