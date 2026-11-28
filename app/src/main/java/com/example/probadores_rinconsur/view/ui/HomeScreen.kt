package com.example.probadores_rinconsur.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.probadores_rinconsur.navegations.Routes
import com.example.probadores_rinconsur.navegations.menuItems
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.probadores_rinconsur.viewmodels.ClimaViewModel

@Composable
fun HomeScreen(
    userName: String,
    esAdmin: Boolean,
    climaViewModel: ClimaViewModel,
    onNavigate: (String) -> Unit
) {
    val clima by climaViewModel.estado.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hola, $userName", style = MaterialTheme.typography.headlineMedium)
        Text("¿Qué deseas hacer?", style = MaterialTheme.typography.bodyLarge)

        // API EXTERNA (Open-Meteo): clima actual de Santiago.
        // Si falla, solo se muestra un aviso y el resto de la app sigue funcionando.
        Card(
            onClick = { climaViewModel.cargarClima() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Clima en Santiago", style = MaterialTheme.typography.titleMedium)
                    Text(
                        when {
                            clima.cargando -> "Consultando el clima..."
                            clima.temperatura != null ->
                                "${clima.temperatura}°C · ${clima.descripcion}"
                            else -> (clima.error ?: "Toca para reintentar") + " (toca para reintentar)"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Accesos rápidos (mismos destinos que el menú lateral, excepto Inicio).
        // Las opciones solo para administrador no se muestran al Personal.
        menuItems.filter { it.route != Routes.HOME && (esAdmin || !it.soloAdmin) }.forEach { item ->
            Card(
                onClick = { onNavigate(item.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(item.label, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}