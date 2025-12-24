package com.example.pm_capturar_foto

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.pm_capturar_foto.ui.theme.Pm_capturar_fotoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.util.concurrent.Executor

// --- Actividad Principal de la Aplicación ---
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita el modo "edge-to-edge" para que la UI ocupe toda la pantalla,
        // incluyendo el área de las barras de estado y navegación.
        enableEdgeToEdge()
        // Define el contenido de la actividad usando Jetpack Compose.
        setContent {
            // Aplica el tema de la aplicación (colores, tipografía, etc.).
            Pm_capturar_fotoTheme {
                // Surface es un contenedor básico de Material Design.
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Llama al Composable principal que contiene la lógica de la cámara.
                    CamaraView()
                }
            }
        }
    }
}

// --- Composable Principal para la Vista de la Cámara ---
// @OptIn indica que estamos usando una API experimental de la librería Accompanist.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CamaraView() {

    // --- Gestión de Contexto, Ciclo de Vida y Permisos ---

    // Obtiene el contexto actual (necesario para inicializar la cámara y otras APIs de Android).
    val context = LocalContext.current
    // Obtiene el propietario del ciclo de vida (LifecycleOwner), crucial para que CameraX
    // sepa cuándo iniciar, detener y liberar la cámara automáticamente.
    val lifecycleOwner = LocalLifecycleOwner.current

    // Crea un estado para gestionar los permisos de la cámara utilizando la librería Accompanist.
    // En este caso, solo solicitamos el permiso de la cámara.
    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )

    // --- Controlador de la Cámara ---

    // Crea y recuerda una instancia de LifecycleCameraController.
    // Este es un controlador de alto nivel de CameraX que simplifica enormemente su uso.
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            // Habilita explícitamente el caso de uso para capturar imágenes.
            // También se podría habilitar la grabación de vídeo (IMAGE_CAPTURE or VIDEO_CAPTURE).
            setEnabledUseCases(
                LifecycleCameraController.IMAGE_CAPTURE
            )
        }
    }

    // --- Lógica de la UI ---

    // LaunchedEffect se usa para ejecutar una acción (en este caso, solicitar permisos)
    // una sola vez cuando el Composable entra en la composición.
    LaunchedEffect(Unit) {
        permissions.launchMultiplePermissionRequest()
    }

    // Scaffold proporciona una estructura de diseño básica de Material Design (app bars, FABs, etc.).
    Scaffold(
        // Define un botón de acción flotante (FloatingActionButton).
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Al hacer clic, se obtiene el ejecutor del hilo principal.
                    val executor = ContextCompat.getMainExecutor(context)
                    // Llama a la función para tomar y guardar la foto.
                    tomarFotoMediaStore(
                        context = context,
                        cameraController = cameraController,
                        executor = executor
                    )
                }
            ) {
                // Icono para el botón.
                Icon(
                    painter = painterResource(id = R.drawable.icon_camera),
                    contentDescription = "Hacer foto",
                    tint = Color.White
                )
            }
        },
        // Posiciona el FAB en el centro de la parte inferior de la pantalla.
        floatingActionButtonPosition = FabPosition.Center
    ) { padding -> // El padding es proporcionado por Scaffold para evitar que el contenido se solape con el FAB.

        // Comprueba si todos los permisos solicitados han sido concedidos.
        if (permissions.allPermissionsGranted) {
            // Si los permisos están concedidos, muestra la vista previa de la cámara.
            CamaraPreview(
                cameraController = cameraController,
                lifecycleOwner = lifecycleOwner,
                modifier = Modifier.padding(padding)
            )
        } else {
            // Si los permisos son denegados, muestra un mensaje de texto.
            Text(
                text = "Permiso de cámara no concedido",
                modifier = Modifier.padding(padding)
            )
        }
    }
}

// --- Función para Tomar y Guardar una Foto ---
private fun tomarFotoMediaStore(
    context: Context,
    cameraController: LifecycleCameraController,
    executor: Executor
) {
    // 1. Prepara los metadatos de la imagen que se va a guardar.
    val contentValues = ContentValues().apply {
        // Nombre de archivo único basado en la hora actual.
        put(MediaStore.MediaColumns.DISPLAY_NAME, "img_${System.currentTimeMillis()}")
        // Tipo de archivo.
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        // Ruta relativa donde se guardará la imagen en la galería (Pictures/CameraX).
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/CameraX"
        )
    }

    // 2. Crea las opciones de salida para la captura de la imagen.
    //    Aquí se especifica dónde y cómo se guardará la imagen.
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver, // Permite interactuar con el MediaStore.
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // URI principal de la galería de imágenes.
            contentValues // Metadatos de la imagen.
        )
        .build()

    // 3. Llama al método takePicture del controlador de la cámara.
    cameraController.takePicture(
        outputOptions, // Opciones de salida definidas anteriormente.
        executor,      // El hilo en el que se ejecutarán los callbacks.
        // Callback para manejar el resultado de la operación de guardado.
        object : ImageCapture.OnImageSavedCallback {

            // Se llama si la imagen se guarda correctamente.
            override fun onImageSaved(
                outputFileResults: ImageCapture.OutputFileResults
            ) {
                println("Foto guardada correctamente")
                // Muestra la URI donde se guardó la imagen.
                println("URI: ${outputFileResults.savedUri}")
            }

            // Se llama si ocurre un error durante el guardado.
            override fun onError(exception: ImageCaptureException) {
                println("Error al guardar la foto: $exception")
            }
        }
    )
}


// --- Composable para Mostrar la Vista Previa de la Cámara ---
@Composable
fun CamaraPreview(
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    // Vincula el controlador de la cámara al ciclo de vida del Composable.
    // Esto es fundamental para que CameraX gestione la cámara automáticamente.
    cameraController.bindToLifecycle(lifecycleOwner)

    // AndroidView es un Composable que permite integrar Vistas de Android tradicionales
    // (en este caso, PreviewView) dentro de una UI de Jetpack Compose.
    AndroidView(
        modifier = modifier.fillMaxSize(),
        // `factory` es una lambda que crea la Vista de Android. Se llama una sola vez.
        factory = { context ->
            PreviewView(context).apply {
                // Asigna el controlador de la cámara a la PreviewView.
                controller = cameraController
            }
        }
    )
}
