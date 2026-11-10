
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
 *
 * Permite consultar y editar:
 * - Cantidad de prendas
 * - Prenda olvidada
 * - Observaciones
 *
 * Disponible para personal y administrador.
 */
@Composable
fun HistorialScreen(viewModel: HistorialViewModel) {

    val registros by viewModel.registros.collectAsStateWithLifecycle()
    val sedes by viewModel.sedes.collectAsStateWithLifecycle()

    val nombreSede = sedes.associate { it.id to it.nombre }

    // Sede seleccionada en el filtro
    var sedeId by rememberSaveable {
        mutableIntStateOf(0)
    }

    // Registro seleccionado para editar
    var registroSeleccionado by remember {
        mutableStateOf<RegistroOcupacion?>(null)
    }

    // Filtrar historial según sede seleccionada
    val lista = if (sedeId == 0) {
        registros
    } else {
        registros.filter { it.sedeId == sedeId }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // Filtro por sede
        SedeChips(
            sedes,
            sedeId,
            { sedeId = it },
            Modifier.padding(top = 8.dp),
            incluirTodas = true
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            if (lista.isEmpty()) {

                Text(
                    text = "Sin registros.",
                    modifier = Modifier.align(Alignment.Center)
                )

            } else {

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        items = lista,
                        key = { it.id }
                    ) { r ->

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                // Probador, sede y fecha
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    Text(
                                        text = "Probador ${r.numeroProbador} · " +
                                                (nombreSede[r.sedeId]
                                                    ?: "Sede eliminada"),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        text = Formato.fecha(r.inicioMillis),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                // Horarios y cantidad de prendas
                                Text(
                                    text = "${Formato.hora(r.inicioMillis)} - " +
                                            "${Formato.hora(r.finMillis)} · " +
                                            "${Formato.minutosEntre(r.inicioMillis, r.finMillis)} min · " +
                                            "${r.cantidadPrendas} prenda(s)"
                                )

                                // Estado de incidencia
                                if (r.prendaOlvidada) {

                                    Text(
                                        text = "Prenda olvidada",
                                        fontWeight = FontWeight.SemiBold,
                                        color = ColoresOlvidada.principal
                                    )

                                } else {

                                    Text(
                                        text = "Sin incidencia",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                // Mostrar observación si existe
                                if (r.observacion.isNotBlank()) {

                                    Text(
                                        text = "Observación: ${r.observacion}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                // Botón para editar
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

    // Mostrar formulario de edición
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
 * Ventana para editar un registro del historial.
 *
 * Permite modificar cantidad de prendas,
 * incidencia y observación.
 *
 * No modifica las fechas ni los horarios.
 */
@Composable
private fun DialogoEditarRegistro(
    registro: RegistroOcupacion,
    onDismiss: () -> Unit,
    onGuardar: (RegistroOcupacion) -> Unit
) {

    // Cantidad de prendas actual
    var cantidadPrendas by remember(registro.id) {
        mutableStateOf(registro.cantidadPrendas.toString())
    }

    // Estado de prenda olvidada
    var prendaOlvidada by remember(registro.id) {
        mutableStateOf(registro.prendaOlvidada)
    }

    // Observación actual del registro
    var observacion by remember(registro.id) {
        mutableStateOf(registro.observacion)
    }

    // Validar cantidad de prendas
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
                    text = "Probador ${registro.numeroProbador}",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Puedes corregir la cantidad de prendas, " +
                            "indicar si hubo una prenda olvidada " +
                            "y agregar una observación."
                )

                // Modificar cantidad de prendas
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

                // Marcar o desmarcar prenda olvidada
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

                // Agregar o editar observación
                OutlinedTextField(
                    value = observacion,

                    onValueChange = {
                        observacion = it
                    },

                    label = {
                        Text("Observación")
                    },

                    placeholder = {
                        Text(
                            "Ej.: Cliente olvidó una polera negra"
                        )
                    },

                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Text(
                    text = "La fecha y los horarios originales " +
                            "no se modificarán.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },

        confirmButton = {

            Button(
                enabled = cantidadValida,

                onClick = {

                    // Crear copia con los datos modificados
                    val registroEditado = registro.copy(
                        cantidadPrendas = cantidad!!,
                        prendaOlvidada = prendaOlvidada,
                        observacion = observacion.trim()
                    )

                    // Guardar cambios en Room
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
