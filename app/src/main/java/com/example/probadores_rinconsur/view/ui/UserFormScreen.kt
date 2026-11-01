package com.example.probadores_rinconsur.view.ui

import com.example.probadores_rinconsur.view.utils.CameraCapture
import com.example.probadores_rinconsur.view.utils.AnimacionEntradaFormulario
import com.example.probadores_rinconsur.view.utils.PhotoBox
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.probadores_rinconsur.viewmodels.TipoUsuarioViewModel
import com.example.probadores_rinconsur.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    viewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    userId: Int,
    onDone: () -> Unit
) {
    val tipos by tipoUsuarioViewModel.tipos.collectAsStateWithLifecycle()
    var tipoUsuarioId by rememberSaveable { mutableIntStateOf(0) }
    var tipoExpanded by remember { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }
    val dbMessage by viewModel.message.collectAsStateWithLifecycle()

    DisposableEffect(Unit) { onDispose { viewModel.clearMessage() } }

    LaunchedEffect(userId) {
        if (userId > 0 && !loaded) {
            viewModel.getUser(userId)?.let {
                name = it.name
                email = it.email
                password = it.password
                imageUri = it.profileImageUri
                tipoUsuarioId = it.tipoUsuarioId
            }
        }
        loaded = true
        visible = true
    }

    if (showCamera) {
        CameraCapture(
            carpeta = "usuarios",
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
                if (userId > 0) "Editar Usuario" else "Nuevo Usuario",
                style = MaterialTheme.typography.headlineMedium
            )

            PhotoBox(
                imageUri = imageUri,
                circle = true,
                onTakePhoto = { showCamera = true }
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearMessage()
                },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Tipo de usuario (FK)
            ExposedDropdownMenuBox(
                expanded = tipoExpanded,
                onExpandedChange = { tipoExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = tipos
                        .firstOrNull { it.id == tipoUsuarioId }
                        ?.tipoUsuario ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de usuario") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = tipoExpanded
                        )
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
                            }
                        )
                    }
                }
            }

            (error ?: dbMessage)?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    when {
                        name.isBlank() ->
                            error = "El nombre es obligatorio"

                        !Patterns.EMAIL_ADDRESS
                            .matcher(email.trim())
                            .matches() ->
                            error = "Email inválido"

                        password.length < 4 ->
                            error = "La contraseña debe tener al menos 4 caracteres"

                        tipos.none { it.id == tipoUsuarioId } ->
                            error = "Seleccione un tipo de usuario"

                        else -> {
                            error = null
                            viewModel.saveUser(
                                userId,
                                name.trim(),
                                email.trim(),
                                password,
                                imageUri,
                                tipoUsuarioId,
                                onDone
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Usuario")
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