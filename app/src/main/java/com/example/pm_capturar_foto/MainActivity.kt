package com.example.pm_capturar_foto

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.pm_capturar_foto.ui.theme.Pm_capturar_fotoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.util.concurrent.Executor

class MainActivity : ComponentActivity() {
    // Función onCreate: se llama cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        throw UnsupportedOperationException("A implementar por el estudiante")
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CamaraView() {
    throw UnsupportedOperationException("A implementar por el estudiante")

}

@Composable
fun CamaraComposable(
    camaraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier
) {
    throw UnsupportedOperationException("A implementar por el estudiante")
}

// Función para tomar una foto y guardarla en el directorio de imágenes públicas
private fun tomarFoto(
    camaraController: LifecycleCameraController,
    executor: Executor,
    directorio: File
) {
   throw UnsupportedOperationException("A implementar por el estudiante")
}

// Función de previsualización para mostrar la vista de la cámara en el diseñador
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Pm_capturar_fotoTheme {
        CamaraView()
    }
}