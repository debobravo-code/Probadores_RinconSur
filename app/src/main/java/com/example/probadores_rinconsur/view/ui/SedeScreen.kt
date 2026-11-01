package com.example.probadores_rinconsur.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.viewmodels.SedeViewModel

/** CRUD de sedes en una sola pantalla (formulario arriba, lista abajo). */
@Composable
fun SedeScreen(viewModel: SedeViewModel) {
    val sedes by viewModel.sedes.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var editId by rememberSaveable { mutableIntStateOf(0) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var alerta by rememberSaveable { mutableStateOf("15") }
    var toDelete by remember { mutableStateOf<Sede?>(null) }

    DisposableEffect(Unit) { onDispose { viewModel.clearMessage() } }

    fun limpiar() {
        editId = 0
        nombre = ""
        alerta = "15"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (editId > 0) "Editar sede" else "Nueva sede",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                if (it.length <= Sede.MAX_LARGO) nombre = it
                viewModel.clearMessage()
            },
            label = { Text("Nombre de la sede") },
            supportingText = { Text("${nombre.length}/${Sede.MAX_LARGO}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = alerta,
            onValueChange = {
                if (it.length <= 2) alerta = it
                viewModel.clearMessage()
            },
            label = { Text("Tiempo de alerta (minutos)") },
            supportingText = {
                Text(
                    "Entre ${Sede.ALERTA_MIN} y ${Sede.ALERTA_MAX}. " +
                            "Solo es un aviso para el personal."
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        message?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.save(editId, nombre, alerta) {
                        limpiar()
                    }
                }
            ) {
                Text(
                    if (editId > 0) "Actualizar" else "Agregar"
                )
            }

            if (editId > 0) {
                OutlinedButton(
                    onClick = { limpiar() }
                ) {
                    Text("Cancelar")
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sedes, key = { it.id }) { sede ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${sede.id}. ${sede.nombre}")

                            Text(
                                "Alerta: ${sede.tiempoAlerta} min",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton(
                            onClick = {
                                editId = sede.id
                                nombre = sede.nombre
                                alerta = sede.tiempoAlerta.toString()
                            }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar"
                            )
                        }

                        IconButton(
                            onClick = { toDelete = sede }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar"
                            )
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { sede ->
        AlertDialog(
            onDismissRequest = {
                toDelete = null
            },
            title = {
                Text("Eliminar sede")
            },
            text = {
                Text("¿Eliminar \"${sede.nombre}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(sede)
                        toDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        toDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}