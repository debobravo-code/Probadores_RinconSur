package com.example.probadores_rinconsur.view.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavBackStackEntry

/*
 * Animaciones de la app, reunidas en un solo lugar.
 *   ANIMACIÓN 1: AnimacionEntradaFormulario  -> ProbadorFormScreen, ReporteFormScreen, UserFormScreen
 *   ANIMACIÓN 2: Modifier.pulsoYRotacion()   -> logo de LoginScreen
 *   ANIMACIÓN 3: transiciones de navegación  -> NavHost en AppNavigation
 */

// ---------------------------------------------------------------------------
// ANIMACIÓN 1: el contenido aparece con fade + deslizamiento vertical
// ---------------------------------------------------------------------------
@Composable
fun AnimacionEntradaFormulario(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 },
        content = content
    )
}

// ---------------------------------------------------------------------------
// ANIMACIÓN 2: animación infinita (pulso de escala + rotación suave)
// Uso: Modifier.size(96.dp).pulsoYRotacion()
// ---------------------------------------------------------------------------
@Composable
fun Modifier.pulsoYRotacion(): Modifier {
    val transition = rememberInfiniteTransition(label = "pulsoYRotacion")
    val scale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "scale"
    )
    val rotation by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            tween(1400),
            RepeatMode.Reverse
        ),
        label = "rotation"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        rotationZ = rotation
    }
}

// ---------------------------------------------------------------------------
// ANIMACIÓN 3: transiciones de deslizamiento horizontal entre pantallas (NavHost)
// ---------------------------------------------------------------------------
private const val DURACION_NAV = 400

val navEnterTransition:
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        tween(DURACION_NAV)
    ) + fadeIn(tween(DURACION_NAV))
}

val navExitTransition:
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        tween(DURACION_NAV)
    ) + fadeOut(tween(DURACION_NAV))
}

val navPopEnterTransition:
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        tween(DURACION_NAV)
    ) + fadeIn(tween(DURACION_NAV))
}

val navPopExitTransition:
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        tween(DURACION_NAV)
    ) + fadeOut(tween(DURACION_NAV))
}

// ---------------------------------------------------------------------------
// EXTRA DE RINCÓN SUR (no está en la plantilla): parpadeo del punto de un probador con alerta
// Uso: Modifier.size(14.dp).parpadeo()
// ---------------------------------------------------------------------------
@Composable
fun Modifier.parpadeo(): Modifier {
    val transition = rememberInfiniteTransition(label = "parpadeo")
    val alfa by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            tween(700),
            RepeatMode.Reverse
        ),
        label = "alfa"
    )
    return this.graphicsLayer { this.alpha = alfa }
}