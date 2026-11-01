package com.example.probadores_rinconsur.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.probadores_rinconsur.repository.models.TipoUsuario
import com.example.probadores_rinconsur.viewmodels.TipoUsuarioViewModel

/** CRUD de tipos de usuario en una sola pantalla (formulario arriba, lista abajo). */
@Composable
fun TipoUsuarioScreen(viewModel: TipoUsuarioViewModel) {
    val tipos by viewModel.tipos.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var editId by rememberSaveable { mutableIntStateOf(0) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var toDelete by remember { mutableStateOf<TipoUsuario?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (editId > 0) "Editar tipo de usuario" else "Nuevo tipo de usuario",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                if (it.length <= TipoUsuario.MAX_LARGO) nombre = it
                viewModel.clearMessage()
            },
            label = { Text("Tipo de usuario") },
            supportingText = {
                Text("${nombre.length}/${TipoUsuario.MAX_LARGO}")
            },
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
                    viewModel.save(editId, nombre) {
                        editId = 0
                        nombre = ""
                    }
                }
            ) {
                Text(
                    if (editId > 0) "Actualizar" else "Agregar"
                )
            }

            if (editId > 0) {
                OutlinedButton(
                    onClick = {
                        editId = 0
                        nombre = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tipos, key = { it.id }) { tipo ->
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
                        Text(
                            "${tipo.id}. ${tipo.tipoUsuario}",
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                editId = tipo.id
                                nombre = tipo.tipoUsuario
                            }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar"
                            )
                        }

                        IconButton(
                            onClick = {
                                toDelete = tipo
                            }
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

    toDelete?.let { tipo ->
        AlertDialog(
            onDismissRequest = {
                toDelete = null
            },
            title = {
                Text("Eliminar tipo")
            },
            text = {
                Text("¿Eliminar \"${tipo.tipoUsuario}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(tipo)
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