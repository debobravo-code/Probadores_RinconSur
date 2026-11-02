package com.example.probadores_rinconsur.view.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.probadores_rinconsur.repository.models.EstadoProbador
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.view.theme.ColoresAlerta
import com.example.probadores_rinconsur.view.theme.ColoresEstado
import com.example.probadores_rinconsur.view.theme.ColoresLibre
import com.example.probadores_rinconsur.view.theme.ColoresOcupado
import com.example.probadores_rinconsur.view.theme.coloresDeEstado
import com.example.probadores_rinconsur.view.utils.Formato
import com.example.probadores_rinconsur.view.utils.SedeChips
import com.example.probadores_rinconsur.view.utils.parpadeo
import com.example.probadores_rinconsur.viewmodels.ProbadorViewModel
import com.example.probadores_rinconsur.viewmodels.UserViewModel
import kotlinx.coroutines.delay

/**
 * Panel de probadores de una sede.
 *
 * Administrador:
 * - Puede ver todas las sedes.
 * - Puede agregar, editar y eliminar probadores.
 *
 * Personal:
 * - Solo puede ver las sedes que tiene asignadas.
 * - Puede ocupar, finalizar y gestionar incidencias.
 */
@Composable
fun ProbadorListScreen(
    viewModel: ProbadorViewModel,
    userViewModel: UserViewModel,
    currentUserId: Int?,
    esAdmin: Boolean,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onReportar: (Int) -> Unit
) {

    val probadores by viewModel.probadores.collectAsStateWithLifecycle()
    val todasLasSedes by viewModel.sedes.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    // Para Personal contiene solamente sus sedes asignadas.
    var sedesPersonal by remember {
        mutableStateOf<List<Sede>>(emptyList())
    }

    // Indica si ya terminamos de consultar las sedes del Personal.
    var sedesPersonalCargadas by remember {
        mutableStateOf(false)
    }

    var sedeId by rememberSaveable {
        mutableIntStateOf(0)
    }

    var toOcupar by remember {
        mutableStateOf<Probador?>(null)
    }

    var prendasSel by remember {
        mutableIntStateOf(1)
    }

    var toFinalize by remember {
        mutableStateOf<Probador?>(null)
    }

    var toDelete by remember {
        mutableStateOf<Probador?>(null)
    }

    val snackbar = remember {
        SnackbarHostState()
    }

    var ahora by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    // Actualiza los minutos de ocupación.
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            ahora = System.currentTimeMillis()
        }
    }

    // Si es Personal, cargamos únicamente sus sedes asignadas.
    LaunchedEffect(
        currentUserId,
        esAdmin
    ) {

        if (esAdmin) {

            sedesPersonal = emptyList()
            sedesPersonalCargadas = true

        } else {

            sedesPersonalCargadas = false

            sedesPersonal =
                if (currentUserId != null) {
                    userViewModel.getSedesAsignadas(
                        currentUserId
                    )
                } else {
                    emptyList()
                }

            sedesPersonalCargadas = true
        }
    }

    // Administrador ve todas.
    // Personal ve solamente las asignadas.
    val sedesDisponibles =
        if (esAdmin) {
            todasLasSedes
        } else {
            sedesPersonal
        }

    // Seleccionamos automáticamente la primera sede disponible.
    // Si tiene varias, puede cambiar usando los chips.
    LaunchedEffect(sedesDisponibles) {

        if (
            sedesDisponibles.isNotEmpty() &&
            sedesDisponibles.none {
                it.id == sedeId
            }
        ) {
            sedeId =
                sedesDisponibles.first().id
        }

        if (sedesDisponibles.isEmpty()) {
            sedeId = 0
        }
    }

    LaunchedEffect(message) {

        message?.let {

            snackbar.showSnackbar(it)

            viewModel.clearMessage()
        }
    }

    val sede =
        sedesDisponibles.firstOrNull {
            it.id == sedeId
        }

    val tiempoAlerta =
        sede?.tiempoAlerta ?: 15

    val lista =
        probadores.filter {
            it.sedeId == sedeId
        }

    fun enAlerta(
        p: Probador
    ): Boolean {

        val inicio =
            p.inicioOcupacion ?: return false

        return p.estado ==
                EstadoProbador.OCUPADO &&
                Formato.minutosEntre(
                    inicio,
                    ahora
                ) >= tiempoAlerta
    }

    val libres =
        lista.count {
            it.estado ==
                    EstadoProbador.LIBRE
        }

    val ocupados =
        lista.count {
            it.estado ==
                    EstadoProbador.OCUPADO
        }

    val conAlerta =
        lista.count {
            enAlerta(it)
        }

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        when {

            // Esperamos mientras se consultan las asignaciones.
            !esAdmin &&
                    !sedesPersonalCargadas -> {

                Text(
                    text =
                        "Cargando sedes asignadas...",
                    modifier =
                        Modifier.align(
                            Alignment.Center
                        )
                )
            }

            // Mensaje correcto para Personal.
            !esAdmin &&
                    sedesDisponibles.isEmpty() -> {

                Text(
                    text =
                        "No tienes sedes asignadas. Contacta al administrador.",
                    modifier =
                        Modifier
                            .align(
                                Alignment.Center
                            )
                            .padding(24.dp)
                )
            }

            // Mensaje para Administrador.
            esAdmin &&
                    sedesDisponibles.isEmpty() -> {

                Text(
                    text =
                        "No hay sedes. Crea una en el menú Sedes.",
                    modifier =
                        Modifier
                            .align(
                                Alignment.Center
                            )
                            .padding(24.dp)
                )
            }

            else -> {

                Column(
                    modifier =
                        Modifier.fillMaxSize()
                ) {

                    // Si existen varias sedes disponibles,
                    // el usuario puede elegir dónde está trabajando.
                    SedeChips(
                        sedesDisponibles,
                        sedeId,
                        {
                            sedeId = it
                        },
                        Modifier.padding(
                            top = 8.dp
                        )
                    )

                    LazyColumn(
                        contentPadding =
                            PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 88.dp
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {

                        item {

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        8.dp
                                    )
                            ) {

                                Resumen(
                                    "Libres",
                                    libres,
                                    ColoresLibre,
                                    Modifier.weight(1f)
                                )

                                Resumen(
                                    "Ocupados",
                                    ocupados,
                                    ColoresOcupado,
                                    Modifier.weight(1f)
                                )

                                Resumen(
                                    "Con alerta",
                                    conAlerta,
                                    ColoresAlerta,
                                    Modifier.weight(1f)
                                )
                            }
                        }

                        if (lista.isEmpty()) {

                            item {

                                Text(
                                    "Esta sede no tiene probadores."
                                )
                            }
                        }

                        items(
                            lista,
                            key = {
                                it.id
                            }
                        ) { p ->

                            ProbadorCard(
                                p = p,
                                ahora = ahora,
                                alerta =
                                    enAlerta(p),
                                esAdmin =
                                    esAdmin,
                                onOcupar = {
                                    prendasSel = 1
                                    toOcupar = p
                                },
                                onFinalizar = {
                                    toFinalize = p
                                },
                                onSolucionar = {
                                    viewModel
                                        .solucionarPrenda(p)
                                },
                                onReportar = {
                                    onReportar(p.id)
                                },
                                onEdit = {
                                    onEdit(p.id)
                                },
                                onDelete = {
                                    toDelete = p
                                }
                            )
                        }
                    }
                }
            }
        }

        // Solo Administrador puede crear probadores.
        if (esAdmin) {

            FloatingActionButton(
                onClick = onAdd,
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomEnd
                        )
                        .padding(16.dp)
            ) {

                Icon(
                    Icons.Default.Add,
                    contentDescription =
                        "Agregar probador"
                )
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier =
                Modifier.align(
                    Alignment.BottomCenter
                )
        )
    }

    // OCUPAR PROBADOR
    toOcupar?.let { p ->

        AlertDialog(
            onDismissRequest = {
                toOcupar = null
            },
            title = {
                Text(
                    "Ocupar Probador ${p.numero}"
                )
            },
            text = {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {

                    Text(
                        "¿Cuántas prendas ingresa la persona? " +
                                "(máximo ${Probador.MAX_PRENDAS})"
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            )
                    ) {

                        (1..Probador.MAX_PRENDAS)
                            .forEach { n ->

                                FilterChip(
                                    selected =
                                        prendasSel == n,
                                    onClick = {
                                        prendasSel = n
                                    },
                                    label = {
                                        Text("$n")
                                    }
                                )
                            }
                    }
                }
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        viewModel.ocupar(
                            p,
                            prendasSel
                        )

                        toOcupar = null
                    }
                ) {

                    Text("Ocupar")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        toOcupar = null
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }

    // FINALIZAR USO
    toFinalize?.let { p ->

        AlertDialog(
            onDismissRequest = {
                toFinalize = null
            },
            title = {
                Text("Finalizar uso")
            },
            text = {

                Text(
                    "¿Quedó alguna prenda olvidada " +
                            "en el Probador ${p.numero}?"
                )
            },
            dismissButton = {

                TextButton(
                    onClick = {

                        viewModel.finalizar(
                            p,
                            quedoPrenda = false
                        )

                        toFinalize = null
                    }
                ) {

                    Text(
                        "No, está vacío"
                    )
                }
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        viewModel.finalizar(
                            p,
                            quedoPrenda = true
                        )

                        toFinalize = null

                        onReportar(p.id)
                    }
                ) {

                    Text(
                        "Sí, quedó una"
                    )
                }
            }
        )
    }

    // ELIMINAR PROBADOR
    toDelete?.let { p ->

        AlertDialog(
            onDismissRequest = {
                toDelete = null
            },
            title = {
                Text("Eliminar probador")
            },
            text = {
                Text(
                    "¿Eliminar el Probador ${p.numero}?"
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        viewModel
                            .deleteProbador(p)

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

@Composable
private fun Resumen(
    etiqueta: String,
    cantidad: Int,
    colores: ColoresEstado,
    modifier: Modifier
) {

    Column(
        modifier
            .clip(
                RoundedCornerShape(12.dp)
            )
            .background(
                colores.fondo
            )
            .padding(
                vertical = 10.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            "$cantidad",
            fontSize = 26.sp,
            fontWeight =
                FontWeight.Bold,
            color =
                colores.principal
        )

        Text(
            etiqueta,
            fontSize = 12.sp,
            color =
                colores.texto
        )
    }
}

@Composable
private fun ProbadorCard(
    p: Probador,
    ahora: Long,
    alerta: Boolean,
    esAdmin: Boolean,
    onOcupar: () -> Unit,
    onFinalizar: () -> Unit,
    onSolucionar: () -> Unit,
    onReportar: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val colores =
        coloresDeEstado(
            p.estado,
            alerta
        )

    val fondo by animateColorAsState(
        colores.fondo,
        label = "fondo"
    )

    val minutos =
        p.inicioOcupacion?.let {
            Formato.minutosEntre(
                it,
                ahora
            )
        } ?: 0

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    fondo,
                contentColor =
                    colores.texto
            )
    ) {

        Column(
            modifier =
                Modifier.padding(12.dp),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    Modifier
                        .size(14.dp)
                        .then(
                            if (alerta) {
                                Modifier.parpadeo()
                            } else {
                                Modifier
                            }
                        )
                        .background(
                            colores.principal,
                            CircleShape
                        )
                )

                Spacer(
                    Modifier.width(12.dp)
                )

                Text(
                    "Probador ${p.numero}",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    modifier =
                        Modifier.weight(1f)
                )

                Column(
                    horizontalAlignment =
                        Alignment.End
                ) {

                    Text(
                        p.estado
                            .etiqueta
                            .uppercase(),
                        fontSize = 12.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    if (
                        p.estado ==
                        EstadoProbador.OCUPADO
                    ) {

                        Text(
                            "$minutos min · " +
                                    "${p.cantidadPrendas} prenda(s)",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.End,
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                when (p.estado) {

                    EstadoProbador.LIBRE ->

                        Button(
                            onClick =
                                onOcupar
                        ) {

                            Text("Ocupar")
                        }

                    EstadoProbador.OCUPADO ->

                        Button(
                            onClick =
                                onFinalizar
                        ) {

                            Text(
                                "Finalizar uso"
                            )
                        }

                    EstadoProbador.PRENDA_OLVIDADA -> {

                        IconButton(
                            onClick =
                                onReportar
                        ) {

                            Icon(
                                Icons.Default.Description,
                                contentDescription =
                                    "Registrar reporte"
                            )
                        }

                        Button(
                            onClick =
                                onSolucionar
                        ) {

                            Text(
                                "Incidencia solucionada"
                            )
                        }
                    }

                    EstadoProbador.FUERA_DE_SERVICIO ->

                        Text(
                            "No disponible",
                            fontSize = 12.sp
                        )
                }

                if (esAdmin) {

                    IconButton(
                        onClick =
                            onEdit
                    ) {

                        Icon(
                            Icons.Default.Edit,
                            contentDescription =
                                "Editar"
                        )
                    }

                    IconButton(
                        onClick =
                            onDelete
                    ) {

                        Icon(
                            Icons.Default.Delete,
                            contentDescription =
                                "Eliminar",
                            tint =
                                Color(
                                    0xFFC62828
                                )
                        )
                    }
                }
            }
        }
    }
}