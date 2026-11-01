package com.example.probadores_rinconsur.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.example.probadores_rinconsur.repository.models.EstadoProbador
import com.example.probadores_rinconsur.view.utils.AnimacionEntradaFormulario
import com.example.probadores_rinconsur.viewmodels.ProbadorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProbadorFormScreen(
    viewModel: ProbadorViewModel,
    probadorId: Int,
    onDone: () -> Unit
) {
    val sedes by viewModel.sedes.collectAsStateWithLifecycle()
    val dbMessage by viewModel.message.collectAsStateWithLifecycle()

    var numero by rememberSaveable { mutableStateOf("") }
    var sedeId by rememberSaveable { mutableIntStateOf(0) }
    var habilitado by rememberSaveable { mutableStateOf(true) }
    var bloqueado by rememberSaveable { mutableStateOf(false) }
    var estadoActual by rememberSaveable { mutableStateOf("") }
    var sedeExpanded by remember { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearMessage() }
    }

    // Carga el probador a editar mediante una corrutina
    LaunchedEffect(probadorId) {
        if (probadorId > 0 && !loaded) {
            viewModel.getProbador(probadorId)?.let {
                numero = it.numero.toString()
                sedeId = it.sedeId
                habilitado = it.estado != EstadoProbador.FUERA_DE_SERVICIO
                bloqueado =
                    it.estado == EstadoProbador.OCUPADO ||
                            it.estado == EstadoProbador.PRENDA_OLVIDADA
                estadoActual = it.estado.etiqueta
            }
        }
        loaded = true
        visible = true
    }

    // ANIMACIÓN 1: fade + deslizamiento vertical
    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (probadorId > 0) "Editar Probador" else "Nuevo Probador",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = numero,
                onValueChange = {
                    numero = it
                    viewModel.clearMessage()
                },
                label = { Text("Número de probador") },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Sede (FK)
            ExposedDropdownMenuBox(
                expanded = sedeExpanded,
                onExpandedChange = { sedeExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value =
                        sedes.firstOrNull { it.id == sedeId }?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sede") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = sedeExpanded
                        )
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = sedeExpanded,
                    onDismissRequest = { sedeExpanded = false }
                ) {
                    sedes.forEach { sede ->
                        DropdownMenuItem(
                            text = { Text(sede.nombre) },
                            onClick = {
                                sedeId = sede.id
                                sedeExpanded = false
                            }
                        )
                    }
                }
            }

            // Habilitado / fuera de servicio
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (bloqueado) {
                        "Estado actual: $estadoActual"
                    } else if (habilitado) {
                        "Habilitado"
                    } else {
                        "Fuera de servicio"
                    },
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = habilitado,
                    onCheckedChange = { habilitado = it },
                    enabled = !bloqueado
                )
            }

            (error ?: dbMessage)?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    val n = numero.trim().toIntOrNull()

                    when {
                        n == null || n <= 0 ->
                            error =
                                "El número de probador debe ser mayor que 0"

                        sedes.none { it.id == sedeId } ->
                            error = "Seleccione una sede"

                        else -> {
                            error = null
                            viewModel.saveProbador(
                                probadorId,
                                n,
                                sedeId,
                                habilitado,
                                onDone
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Probador")
            }

            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}