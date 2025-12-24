package com.example.pm_capturar_foto

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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

class MainActivity : ComponentActivity() {
    // Función onCreate: se llama cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita la barra de aplicaciones para que se extienda hasta los bordes de la pantalla
        enableEdgeToEdge()
        // Configura el contenido de la actividad
        setContent {
            // Crea un tema de la aplicación y lo aplica
            Pm_capturar_fotoTheme {
                // Crea una superficie que rellena todo el espacio disponible y utiliza el color de fondo del tema
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // Muestra la vista de la cámara
                    CamaraView()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CamaraView() {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                LifecycleCameraController.IMAGE_CAPTURE
            )
        }
    }

    LaunchedEffect(Unit) {
        permissions.launchMultiplePermissionRequest()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val executor = ContextCompat.getMainExecutor(context)
                    tomarFotoMediaStore(
                        context = context,
                        cameraController = cameraController,
                        executor = executor
                    )
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_camera),
                    contentDescription = "Hacer foto",
                    tint = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->

        if (permissions.allPermissionsGranted) {
            CamaraPreview(
                cameraController = cameraController,
                lifecycleOwner = lifecycleOwner,
                modifier = Modifier.padding(padding)
            )
        } else {
            Text(
                text = "Permiso de cámara no concedido",
                modifier = Modifier.padding(padding)
            )
        }
    }
}

private fun tomarFotoMediaStore(
    context: Context,
    cameraController: LifecycleCameraController,
    executor: Executor
) {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "img_${System.currentTimeMillis()}")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/CameraX"
        )
    }

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    cameraController.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {

            override fun onImageSaved(
                outputFileResults: ImageCapture.OutputFileResults
            ) {
                println("Foto guardada correctamente")
                println("URI: ${outputFileResults.savedUri}")
            }

            override fun onError(exception: ImageCaptureException) {
                println("Error al guardar la foto: $exception")
            }
        }
    )
}


// Función de previsualización para mostrar la vista de la cámara en el diseñador
@Composable
fun CamaraPreview(
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    cameraController.bindToLifecycle(lifecycleOwner)

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PreviewView(context).apply {
                controller = cameraController
            }
        }
    )
}
