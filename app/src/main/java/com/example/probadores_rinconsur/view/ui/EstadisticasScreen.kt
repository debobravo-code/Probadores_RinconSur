package com.example.probadores_rinconsur.view.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.view.utils.SedeChips
import com.example.probadores_rinconsur.viewmodels.EstadisticasViewModel
import com.example.probadores_rinconsur.viewmodels.ResumenPrendas

/** Estadísticas de prendas por persona. Solo el Administrador ve esta pantalla (menú soloAdmin). */
@Composable
fun EstadisticasScreen(viewModel: EstadisticasViewModel) {
    val registros by viewModel.registros.collectAsStateWithLifecycle()
    val sedes by viewModel.sedes.collectAsStateWithLifecycle()
    var sedeId by rememberSaveable { mutableIntStateOf(0) }

    val resumen = viewModel.resumen(registros, sedeId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SedeChips(sedes, sedeId, { sedeId = it }, incluirTodas = true)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tres números principales
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Dato("Usos", "${resumen.usos}", Modifier.weight(1f))
                Dato("Prendas", "${resumen.totalPrendas}", Modifier.weight(1f))
                Dato(
                    "Promedio",
                    "%.1f".format(resumen.promedio),
                    Modifier.weight(1f)
                )
            }

            if (resumen.usos == 0) {
                Text(
                    "Aún no hay usos con prendas registradas.",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Distribucion(resumen)
            }

            // Comparación entre sedes (solo cuando se ven todas)
            if (sedeId == 0 && sedes.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Promedio por sede",
                            style = MaterialTheme.typography.titleMedium
                        )

                        sedes.forEach { sede ->
                            val r = viewModel.resumen(registros, sede.id)

                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    sede.nombre,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    if (r.usos == 0) {
                                        "Sin datos"
                                    } else {
                                        "%.1f prendas (%d usos)".format(
                                            r.promedio,
                                            r.usos
                                        )
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta con un número grande y su etiqueta
@Composable
private fun Dato(
    etiqueta: String,
    valor: String,
    modifier: Modifier
) {
    Card(modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                valor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                etiqueta,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

// Barras horizontales: cuántas personas se probaron 1, 2, 3 o 4 prendas
@Composable
private fun Distribucion(resumen: ResumenPrendas) {
    val maximo =
        (resumen.distribucion.maxOrNull() ?: 0).coerceAtLeast(1)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "¿Cuántas prendas se prueba la gente?",
                style = MaterialTheme.typography.titleMedium
            )

            resumen.distribucion.forEachIndexed { i, cantidad ->
                val n = i + 1

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (n == 1) "1 prenda" else "$n prendas",
                        modifier = Modifier.width(76.dp)
                    )

                    Box(
                        Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(
                                    cantidad.toFloat() / maximo
                                )
                                .height(16.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    val pct = resumen.usos.let {
                        if (it == 0) 0
                        else cantidad * 100 / it
                    }

                    Text(
                        "$cantidad ($pct%)",
                        modifier = Modifier.width(72.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                "Máximo permitido por persona: ${Probador.MAX_PRENDAS} prendas",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}