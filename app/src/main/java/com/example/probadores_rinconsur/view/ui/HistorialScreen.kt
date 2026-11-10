
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.view.theme.ColoresOlvidada
import com.example.probadores_rinconsur.view.utils.Formato
import com.example.probadores_rinconsur.view.utils.SedeChips
import com.example.probadores_rinconsur.viewmodels.HistorialViewModel

/**
 * Historial de usos finalizados.
 * Permite consultar y editar registros de ocupación.
 * Disponible para personal y administrador.
 */
@Composable
fun HistorialScreen(viewModel: HistorialViewModel) {

    val registros by viewModel.registros.collectAsStateWithLifecycle()
    val sedes by viewModel.sedes.collectAsStateWithLifecycle()

    val nombreSede = sedes.associate { it.id to it.nombre }

    var sedeId by rememberSaveable {
        mutableIntStateOf(0)
    }

    // Registro seleccionado para editar
    var registroSeleccionado by remember {
        mutableStateOf<RegistroOcupacion?>(null)
    }

    val lista = if (sedeId == 0) {
        registros
    } else {
        registros.filter { it.sedeId == sedeId }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Filtro por sede
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

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    Text(
                                        "Probador ${r.numeroProbador} · " +
                                                (nombreSede[r.sedeId] ?: "Sede eliminada"),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        Formato.fecha(r.inicioMillis),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                // Horario y cantidad de prendas
                                Text(
                                    "${Formato.hora(r.inicioMillis)} - " +
                                            "${Formato.hora(r.finMillis)} · " +
                                            "${Formato.minutosEntre(r.inicioMillis, r.finMillis)} min · " +
                                            "${r.cantidadPrendas} prenda(s)"
                                )

                                // Estado de incidencia
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

                                // Botón para editar el registro
                                OutlinedButton(
                                    onClick = {
                                        registroSeleccionado = r
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Editar registro")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Abrir formulario de edición
    registroSeleccionado?.let { registro ->

        DialogoEditarRegistro(
            registro = registro,
            onDismiss = {
                registroSeleccionado = null
            },
            onGuardar = { registroEditado ->

                viewModel.actualizarRegistro(registroEditado)

                registroSeleccionado = null
            }
        )
    }
}

/**
 * Ventana para editar la cantidad de prendas
 * y el estado de incidencia.
 */
@Composable
private fun DialogoEditarRegistro(
    registro: RegistroOcupacion,
    onDismiss: () -> Unit,
    onGuardar: (RegistroOcupacion) -> Unit
) {

    var cantidadPrendas by remember(registro.id) {
        mutableStateOf(registro.cantidadPrendas.toString())
    }

    var prendaOlvidada by remember(registro.id) {
        mutableStateOf(registro.prendaOlvidada)
    }

    val cantidad = cantidadPrendas.toIntOrNull()
    val cantidadValida = cantidad != null && cantidad >= 0

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Editar registro")
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    "Probador ${registro.numeroProbador}",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "Puedes corregir la cantidad de prendas " +
                            "y el estado de incidencia."
                )

                // Campo para modificar cantidad de prendas
                OutlinedTextField(
                    value = cantidadPrendas,
                    onValueChange = { nuevoValor ->
                        cantidadPrendas = nuevoValor
                    },
                    label = {
                        Text("Cantidad de prendas")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    isError = !cantidadValida,
                    supportingText = {
                        if (!cantidadValida) {
                            Text("Ingresa una cantidad válida (0 o más).")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Marcar o desmarcar incidencia
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = prendaOlvidada,
                        onCheckedChange = {
                            prendaOlvidada = it
                        }
                    )

                    Text("Prenda olvidada")
                }

                Text(
                    "La fecha y los horarios originales no se modificarán.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },

        confirmButton = {

            Button(
                enabled = cantidadValida,
                onClick = {

                    val registroEditado = registro.copy(
                        cantidadPrendas = cantidad!!,
                        prendaOlvidada = prendaOlvidada
                    )

                    onGuardar(registroEditado)
                }
            ) {
                Text("Guardar cambios")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}
