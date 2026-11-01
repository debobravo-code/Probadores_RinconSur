package com.example.probadores_rinconsur.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.probadores_rinconsur.repository.models.EstadoProbador

// Azul marino de Rincón Sur
val AzulMarino = Color(0xFF0D3B66)

private val LightColors = lightColorScheme(
    primary = AzulMarino,
    secondary = Color(0xFF2F6FDB),
    tertiary = Color(0xFFF08C00)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFFFE082)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}

// ---------------------------------------------------------------------------
// Colores de los estados de un probador (fondo suave, color principal y texto)
// ---------------------------------------------------------------------------
data class ColoresEstado(val fondo: Color, val principal: Color, val texto: Color)

val ColoresLibre = ColoresEstado(
    Color(0xFFE5F5E9),
    Color(0xFF2E9E4F),
    Color(0xFF1E7B3A)
)

val ColoresOcupado = ColoresEstado(
    Color(0xFFE3EEFC),
    Color(0xFF2F6FDB),
    Color(0xFF2457B8)
)

val ColoresAlerta = ColoresEstado(
    Color(0xFFFDE5E5),
    Color(0xFFE5534B),
    Color(0xFFC62828)
)

val ColoresOlvidada = ColoresEstado(
    Color(0xFFFFF0D9),
    Color(0xFFF08C00),
    Color(0xFFC76A00)
)

val ColoresFuera = ColoresEstado(
    Color(0xFFEDEDED),
    Color(0xFF9E9E9E),
    Color(0xFF555555)
)

/** "Alerta" es un OCUPADO que pasó el tiempo de alerta de su sede. */
fun coloresDeEstado(
    estado: EstadoProbador,
    alerta: Boolean = false
): ColoresEstado = when {
    alerta -> ColoresAlerta
    estado == EstadoProbador.LIBRE -> ColoresLibre
    estado == EstadoProbador.OCUPADO -> ColoresOcupado
    estado == EstadoProbador.PRENDA_OLVIDADA -> ColoresOlvidada
    else -> ColoresFuera
}