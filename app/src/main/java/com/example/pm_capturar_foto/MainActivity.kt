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
    // Solicita los permisos de la cámara
    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )

    // Obtiene el contexto de la actividad
    val context = LocalContext.current
    // Crea un controlador de cámara que se vincula a la actividad
    val camaraController = remember { LifecycleCameraController(context) }
    // Obtiene el propietario de la vida de la actividad
    val lifecycle = LocalLifecycleOwner.current

    // Obtiene el directorio de imágenes públicas
    val directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absoluteFile

    // Verifica los permisos cuando se crea el Composable
    LaunchedEffect(key1 = Unit) {
        permissions.launchMultiplePermissionRequest()
    }

    // Muestra un botón flotante para tomar una foto
    Scaffold(modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Crea un ejecutor para manejar la toma de la foto
                    val executor = ContextCompat.getMainExecutor(context)
                    // Toma una foto y la guarda en el directorio de imágenes públicas
                    tomarFoto(camaraController, executor, directorio)
                }
            ) {
                // Muestra un icono de cámara
                Icon(
                    painterResource(id = R.drawable.icon_camera),
                    tint = Color.White,
                    contentDescription = null
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {
        // Muestra la vista de la cámara si se han concedido los permisos
        if (permissions.allPermissionsGranted) {
            CamaraComposable(
                camaraController = camaraController,
                lifecycleOwner = lifecycle,
                modifier = Modifier.padding(it)
            )
        } else {
            // Muestra un mensaje si no se han concedido los permisos
            Text(text = "Permisos no concedidos", modifier = Modifier.padding(it))
        }
    }

}

@Composable
fun CamaraComposable(
    camaraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier
) {
    // Vincula el controlador de cámara a la vida de la actividad
    camaraController.bindToLifecycle(lifecycleOwner)
    // Muestra la vista de previsualización de la cámara
    AndroidView(
        modifier = modifier,
        factory = {
            val previaView = PreviewView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            previaView.controller = camaraController
            previaView
        }
    )
}

// Función para tomar una foto y guardarla en el directorio de imágenes públicas
private fun tomarFoto(
    camaraController: LifecycleCameraController,
    executor: Executor,
    directorio: File
) {
    // Crea un archivo temporal para la foto
    val image = File.createTempFile("img_", ".png", directorio)
    // Crea opciones para guardar la foto en el archivo temporal
    val outputDirectory = ImageCapture.OutputFileOptions.Builder(image).build()
    // Toma la foto y guarda el resultado en el archivo temporal
    camaraController.takePicture(
        outputDirectory,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            // Función que se llama cuando se guarda la foto
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Muestra un mensaje en la consola para indicar que se ha guardado la foto
                println("foto_tomada")
                // Muestra la URI de la foto guardada en la consola
                println(outputFileResults.savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                println(exception.toString())
            }



        }
    )
}

// Función de previsualización para mostrar la vista de la cámara en el diseñador
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Pm_capturar_fotoTheme {
        CamaraView()
    }
}