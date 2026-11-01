package com.example.probadores_rinconsur.view.ui

import com.example.probadores_rinconsur.view.utils.CameraCapture
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/** Pantalla independiente de cámara accesible desde el menú. */
@Composable
fun CameraScreen() {
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var lastPhoto by rememberSaveable { mutableStateOf<String?>(null) }

    if (showCamera) {
        CameraCapture(
            carpeta = "camara",
            onImageCaptured = { uri: Uri ->
                lastPhoto = uri.toString()
                showCamera = false
            },
            onClose = { showCamera = false }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Cámara (CameraX)",
                style = MaterialTheme.typography.headlineMedium
            )

            if (lastPhoto != null) {
                AsyncImage(
                    model = lastPhoto,
                    contentDescription = "Última foto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                Text("Aún no has tomado fotos.")
            }

            Button(
                onClick = { showCamera = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null
                )
                Text("  Abrir cámara")
            }
        }
    }
}