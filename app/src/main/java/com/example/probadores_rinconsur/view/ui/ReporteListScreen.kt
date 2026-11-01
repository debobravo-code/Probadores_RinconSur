package com.example.probadores_rinconsur.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.probadores_rinconsur.repository.models.ReportePrenda
import com.example.probadores_rinconsur.view.utils.Formato
import com.example.probadores_rinconsur.viewmodels.ReporteViewModel

@Composable
fun ReporteListScreen(
    viewModel: ReporteViewModel,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val reportes by viewModel.reportes.collectAsStateWithLifecycle()
    val probadores by viewModel.probadores.collectAsStateWithLifecycle()
    val sedes by viewModel.sedes.collectAsStateWithLifecycle()
    val probadorPorId = probadores.associateBy { it.id }
    val sedePorId = sedes.associate { it.id to it.nombre }
    var toDelete by remember { mutableStateOf<ReportePrenda?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (reportes.isEmpty()) {
            Text(
                "No hay reportes. Presiona + para agregar.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reportes, key = { it.id }) { reporte ->
                    val probador = probadorPorId[reporte.probadorId]
                    val titulo = if (probador != null)
                        "Probador ${probador.numero} · ${sedePorId[probador.sedeId] ?: ""}"
                    else "Probador eliminado"

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (reporte.fotoUri != null) {
                                AsyncImage(
                                    model = reporte.fotoUri,
                                    contentDescription = "Foto de la prenda",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Icon(
                                    Icons.Default.Checkroom,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    titulo,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    reporte.descripcion.ifBlank {
                                        "Sin descripción"
                                    },
                                    maxLines = 2
                                )
                                Text(
                                    "${Formato.fecha(reporte.fechaMillis)} ${Formato.hora(reporte.fechaMillis)}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            IconButton(
                                onClick = { onEdit(reporte.id) }
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar"
                                )
                            }

                            IconButton(
                                onClick = { toDelete = reporte }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Agregar reporte"
            )
        }
    }

    toDelete?.let { reporte ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar reporte") },
            text = {
                Text("¿Eliminar este reporte de prenda olvidada?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteReporte(reporte)
                        toDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { toDelete = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}