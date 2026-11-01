package com.example.probadores_rinconsur.view.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.probadores_rinconsur.repository.models.Sede

/**
 * Selector de sede (filtro). Con incluirTodas = true se agrega la opción "Todas" (id = 0).
 */
@Composable
fun SedeChips(
    sedes: List<Sede>,
    seleccionada: Int,
    onSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier,
    incluirTodas: Boolean = false
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (incluirTodas) {
            item {
                FilterChip(
                    selected = seleccionada == 0,
                    onClick = { onSeleccionar(0) },
                    label = { Text("Todas") }
                )
            }
        }
        items(sedes, key = { it.id }) { sede ->
            FilterChip(
                selected = seleccionada == sede.id,
                onClick = { onSeleccionar(sede.id) },
                label = { Text(sede.nombre) }
            )
        }
    }
}