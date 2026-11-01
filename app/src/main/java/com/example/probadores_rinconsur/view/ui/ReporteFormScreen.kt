package com.example.probadores_rinconsur.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.probadores_rinconsur.view.utils.AnimacionEntradaFormulario
import com.example.probadores_rinconsur.view.utils.CameraCapture
import com.example.probadores_rinconsur.view.utils.PhotoBox
import com.example.probadores_rinconsur.viewmodels.ReporteViewModel

/**
 * Reporte de prenda olvidada: foto (CameraX), probador (FK) y descripción.
 * probadorIdInicial > 0 precarga el probador (cuando se llega desde el panel de probadores).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteFormScreen(
    viewModel: ReporteViewModel,
    reporteId: Int,
    probadorIdInicial: Int,
    onDone: () -> Unit
) {
    val probadores by viewModel.probadores.collectAsStateWithLifecycle()
    val sedes by viewModel.sedes.collectAsStateWithLifecycle()
    val sedePorId = sedes.associate { it.id to it.nombre }

    var probadorId by rememberSaveable { mutableIntStateOf(probadorIdInicial) }
    var probadorExpanded by remember { mutableStateOf(false) }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var fechaMillis by rememberSaveable { mutableLongStateOf(0L) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }

    // Carga el reporte a editar mediante una corrutina (función suspend del ViewModel)
    LaunchedEffect(reporteId) {
        if (reporteId > 0 && !loaded) {
            viewModel.getReporte(reporteId)?.let {
                probadorId = it.probadorId
                descripcion = it.descripcion
                imageUri = it.fotoUri
                fechaMillis = it.fechaMillis
            }
        }
        loaded = true
        visible = true
    }

    if (showCamera) {
        CameraCapture(
            carpeta = "reportes",
            onImageCaptured = { uri ->
                imageUri = uri.toString()
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
    }

    // ANIMACIÓN 1 (view/utils/Animations.kt): fade + deslizamiento vertical
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
                if (reporteId > 0) "Editar Reporte" else "Registrar prenda olvidada",
                style = MaterialTheme.typography.headlineMedium
            )

            PhotoBox(
                imageUri = imageUri,
                circle = false,
                onTakePhoto = { showCamera = true }
            )

            // Probador (FK): lista desplegable con los registros de probadores
            ExposedDropdownMenuBox(
                expanded = probadorExpanded,
                onExpandedChange = { probadorExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = probadores.firstOrNull { it.id == probadorId }
                        ?.let {
                            "Probador ${it.numero} · ${sedePorId[it.sedeId] ?: ""}"
                        } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Probador") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = probadorExpanded
                        )
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = probadorExpanded,
                    onDismissRequest = { probadorExpanded = false }
                ) {
                    probadores.forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Probador ${p.numero} · ${sedePorId[p.sedeId] ?: ""}"
                                )
                            },
                            onClick = {
                                probadorId = p.id
                                probadorExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (opcional si hay foto)") },
                placeholder = {
                    Text("Ej: polera negra, chaqueta, etc.")
                },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    when {
                        probadores.none { it.id == probadorId } ->
                            error = "Seleccione un probador"

                        descripcion.isBlank() && imageUri == null ->
                            error = "Agregue una descripción o una foto"

                        else -> {
                            error = null
                            viewModel.saveReporte(
                                reporteId,
                                probadorId,
                                descripcion.trim(),
                                imageUri,
                                fechaMillis
                            )
                            onDone()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Reporte")
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