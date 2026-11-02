package com.example.probadores_rinconsur.view.ui

import android.util.Patterns
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.probadores_rinconsur.repository.models.TipoUsuario
import com.example.probadores_rinconsur.view.utils.AnimacionEntradaFormulario
import com.example.probadores_rinconsur.viewmodels.SedeViewModel
import com.example.probadores_rinconsur.viewmodels.TipoUsuarioViewModel
import com.example.probadores_rinconsur.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    viewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    sedeViewModel: SedeViewModel,
    userId: Int,
    onDone: () -> Unit
) {

    val tipos by tipoUsuarioViewModel.tipos.collectAsStateWithLifecycle()
    val sedes by sedeViewModel.sedes.collectAsStateWithLifecycle()
    val dbMessage by viewModel.message.collectAsStateWithLifecycle()

    var tipoUsuarioId by rememberSaveable {
        mutableIntStateOf(0)
    }

    var tipoExpanded by remember {
        mutableStateOf(false)
    }

    var name by rememberSaveable {
        mutableStateOf("")
    }

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    var sedesSeleccionadas by remember {
        mutableStateOf<Set<Int>>(emptySet())
    }

    var error by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var loaded by rememberSaveable {
        mutableStateOf(false)
    }

    var visible by rememberSaveable {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(userId) {

        if (userId > 0 && !loaded) {

            viewModel.getUser(userId)?.let { user ->

                name = user.name
                email = user.email
                password = user.password
                tipoUsuarioId = user.tipoUsuarioId

                val asignadas =
                    viewModel.getSedesAsignadas(userId)

                sedesSeleccionadas =
                    asignadas.map { sede ->
                        sede.id
                    }.toSet()
            }
        }

        loaded = true
        visible = true
    }

    // ANIMACIÓN 1:
    // fade + deslizamiento vertical
    AnimacionEntradaFormulario(
        visible = visible
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    if (userId > 0) {
                        "Editar Usuario"
                    } else {
                        "Nuevo Usuario"
                    },
                style =
                    MaterialTheme.typography.headlineMedium
            )

            // NOMBRE
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    error = null
                },
                label = {
                    Text("Nombre")
                },
                singleLine = true,
                modifier =
                    Modifier.fillMaxWidth()
            )

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    error = null
                    viewModel.clearMessage()
                },
                label = {
                    Text("Email")
                },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Email
                    ),
                modifier =
                    Modifier.fillMaxWidth()
            )

            // CONTRASEÑA
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    error = null
                },
                label = {
                    Text("Contraseña")
                },
                singleLine = true,
                visualTransformation =
                    PasswordVisualTransformation(),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Password
                    ),
                modifier =
                    Modifier.fillMaxWidth()
            )

            // TIPO DE USUARIO
            ExposedDropdownMenuBox(
                expanded = tipoExpanded,
                onExpandedChange = {
                    tipoExpanded = it
                },
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedTextField(
                    value =
                        tipos.firstOrNull {
                            it.id == tipoUsuarioId
                        }?.tipoUsuario ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text("Tipo de usuario")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults
                            .TrailingIcon(
                                expanded = tipoExpanded
                            )
                    },
                    modifier = Modifier
                        .menuAnchor(
                            MenuAnchorType.PrimaryNotEditable
                        )
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = tipoExpanded,
                    onDismissRequest = {
                        tipoExpanded = false
                    }
                ) {

                    tipos.forEach { tipo ->

                        DropdownMenuItem(
                            text = {
                                Text(tipo.tipoUsuario)
                            },
                            onClick = {

                                tipoUsuarioId = tipo.id
                                tipoExpanded = false
                                error = null

                                // Un Administrador no necesita
                                // sedes asignadas.
                                if (
                                    tipo.id ==
                                    TipoUsuario.ID_ADMINISTRADOR
                                ) {
                                    sedesSeleccionadas =
                                        emptySet()
                                }
                            }
                        )
                    }
                }
            }

            // SEDES
            // Solo aparecen cuando el usuario es Personal.
            if (
                tipoUsuarioId != 0 &&
                tipoUsuarioId != TipoUsuario.ID_ADMINISTRADOR
            ) {

                Text(
                    text = "Sedes asignadas",
                    style =
                        MaterialTheme.typography.titleMedium,
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Text(
                    text =
                        "Seleccione una o más sedes donde podrá trabajar este usuario.",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    modifier =
                        Modifier.fillMaxWidth()
                )

                if (sedes.isEmpty()) {

                    Text(
                        text =
                            "No hay sedes registradas. Primero debe crear una sede.",
                        color =
                            MaterialTheme.colorScheme.error,
                        modifier =
                            Modifier.fillMaxWidth()
                    )

                } else {

                    sedes.forEach { sede ->

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked =
                                    sede.id in sedesSeleccionadas,
                                onCheckedChange = { checked ->

                                    sedesSeleccionadas =
                                        if (checked) {

                                            sedesSeleccionadas +
                                                    sede.id

                                        } else {

                                            sedesSeleccionadas -
                                                    sede.id
                                        }

                                    error = null
                                }
                            )

                            Text(
                                text = sede.nombre
                            )
                        }
                    }
                }
            }

            // ERRORES
            (error ?: dbMessage)?.let { mensaje ->

                Text(
                    text = mensaje,
                    color =
                        MaterialTheme.colorScheme.error,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }

            // GUARDAR
            Button(
                onClick = {

                    when {

                        name.isBlank() -> {
                            error =
                                "El nombre es obligatorio"
                        }

                        !Patterns.EMAIL_ADDRESS
                            .matcher(email.trim())
                            .matches() -> {

                            error =
                                "Email inválido"
                        }

                        password.length < 4 -> {

                            error =
                                "La contraseña debe tener al menos 4 caracteres"
                        }

                        tipos.none {
                            it.id == tipoUsuarioId
                        } -> {

                            error =
                                "Seleccione un tipo de usuario"
                        }

                        tipoUsuarioId !=
                                TipoUsuario.ID_ADMINISTRADOR &&
                                sedesSeleccionadas.isEmpty() -> {

                            error =
                                "Debe asignar al menos una sede al personal"
                        }

                        else -> {

                            error = null
                            viewModel.clearMessage()

                            viewModel.saveUser(
                                id = userId,
                                name = name.trim(),
                                email = email.trim(),
                                password = password,
                                tipoUsuarioId =
                                    tipoUsuarioId,
                                sedesAsignadas =
                                    sedesSeleccionadas,
                                onSuccess =
                                    onDone
                            )
                        }
                    }
                },
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text("Guardar Usuario")
            }

            // CANCELAR
            OutlinedButton(
                onClick = onDone,
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text("Cancelar")
            }
        }
    }
}