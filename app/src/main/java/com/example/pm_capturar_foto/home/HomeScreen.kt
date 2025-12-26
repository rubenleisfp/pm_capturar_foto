// Define el paquete donde se encuentra este archivo, una práctica estándar para organizar el código.
package com.example.pm_capturar_foto.home

// Importaciones necesarias de Android y otras librerías.
import android.Manifest
import android.view.ViewGroup
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.util.concurrent.Executor

/**
 * Anotación necesaria porque la librería `accompanist-permissions` todavía se considera experimental.
 * Nos permite usar sus funcionalidades sin que el compilador muestre advertencias.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen() {
    // 1. GESTIÓN DE PERMISOS
    // `rememberPermissionState` crea y recuerda un estado para gestionar el permiso de la cámara.
    // La librería Accompanist se encarga de la lógica de solicitar y verificar el permiso.
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // 2. PREPARACIÓN DE LA CÁMARA Y EL CONTEXTO
    // `LocalContext.current` obtiene el contexto de la aplicación, necesario para inicializar CameraX.
    val context = LocalContext.current
    // `remember` asegura que solo se cree una instancia de `LifecycleCameraController` y se reutilice
    // en las recomposiciones, evitando crear una nueva cámara cada vez que la UI se actualiza.
    val cameraController = remember {
        LifecycleCameraController(context)
    }
    // Obtenemos el `LifecycleOwner` actual. Esto es crucial para que `LifecycleCameraController`
    // sepa cuándo iniciar, detener y liberar la cámara automáticamente (p. ej., cuando la app pasa a segundo plano).
    val lifecycle = LocalLifecycleOwner.current

    // 3. SOLICITUD DE PERMISOS
    // `LaunchedEffect(Unit)` ejecuta el bloque de código una sola vez cuando `HomeScreen` aparece por primera vez.
    // Es el lugar ideal para realizar acciones de inicialización, como solicitar permisos.
    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest() // Lanza el diálogo del sistema para pedir el permiso de la cámara.
    }

    // 4. CONSTRUCCIÓN DE LA INTERFAZ DE USUARIO (UI)
    // `Scaffold` es un layout predefinido que facilita la colocación de elementos comunes como el botón flotante (FAB).
    Scaffold(
        modifier = Modifier.fillMaxSize(), // Ocupa toda la pantalla.
        floatingActionButton = {
            // Define el botón flotante para tomar la foto.
            FloatingActionButton(onClick = {
                // `ContextCompat.getMainExecutor(context)` obtiene un ejecutor que corre en el hilo principal de la UI.
                // CameraX necesita un ejecutor para saber en qué hilo procesar la captura de la foto.
                val executor = ContextCompat.getMainExecutor(context)
                // Llama a nuestra función auxiliar para tomar la foto.
                takePicture(cameraController, executor)
            }) {
                Text(text = "Camara!") // Texto dentro del botón.
            }
        }
    ) {
        // `it` contiene el padding necesario para que el contenido no se solape con elementos del Scaffold como el FAB.

        // 5. LÓGICA CONDICIONAL: ¿TENEMOS PERMISO?
        // Comprobamos el estado del permiso que gestiona Accompanist.
        if (permissionState.status.isGranted) {
            // Si el permiso fue concedido, mostramos la vista previa de la cámara.
            CamaraComposable(cameraController, lifecycle, modifier = Modifier.padding(it))
        } else {
            // Si el permiso fue denegado, mostramos un mensaje al usuario.
            Text(text = "Permiso Denegado!", modifier = Modifier.padding(it))
        }
    }
}

/**
 * Función auxiliar privada que encapsula la lógica para tomar una foto.
 */
private fun takePicture(cameraController: LifecycleCameraController, executor: Executor) {
    // Crea un archivo temporal en el almacenamiento interno de la app para guardar la imagen.
    // Esto es útil para pruebas rápidas sin tener que gestionar el guardado en la galería.
    val file = File.createTempFile("imagentest", ".jpg")
    // `OutputFileOptions` le dice a CameraX dónde y cómo guardar la imagen capturada.
    // En este caso, le decimos que la guarde en el archivo que acabamos de crear.
    val outputDirectory = ImageCapture.OutputFileOptions.Builder(file).build()

    // `cameraController.takePicture()` es el método que inicia la captura de la imagen.
    cameraController.takePicture(
        outputDirectory, // Las opciones de salida (dónde guardar).
        executor,        // El ejecutor para manejar los callbacks en el hilo principal.
        // Un objeto anónimo que implementa `OnImageSavedCallback` para recibir el resultado.
        object : ImageCapture.OnImageSavedCallback {
            // Este método se llama si la foto se guarda correctamente.
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // `outputFileResults.savedUri` contiene la URI del archivo guardado.
                // Imprimimos la URI en la consola para depuración.
                println(outputFileResults.savedUri)
            }

            // Este método se llama si ocurre un error durante la captura o el guardado.
            override fun onError(exception: ImageCaptureException) {
                // Imprimimos el error en la consola para saber qué ha fallado.
                println("Error al tomar la foto: ${exception.message}")
            }
        },
    )
}

/**
 * Composable que se encarga de mostrar la vista previa de la cámara.
 */
@Composable
fun CamaraComposable(
    cameraController: LifecycleCameraController,
    lifecycle: LifecycleOwner,
    modifier: Modifier = Modifier,
) {
    // `bindToLifecycle` es el paso mágico que conecta el controlador de la cámara
    // con el ciclo de vida de la UI. CameraX se encargará de todo automáticamente.
    cameraController.bindToLifecycle(lifecycle)

    // `AndroidView` es un "puente" para usar Vistas del sistema Android (no-Compose) dentro de Compose.
    // Lo usamos para mostrar `PreviewView`, que es la vista que renderiza lo que la cámara ve.
    AndroidView(modifier = modifier, factory = { context ->
        // El bloque `factory` crea la instancia de la Vista de Android. Se ejecuta una sola vez.
        val previewView = PreviewView(context).apply {
            // Configuramos los parámetros de layout para que la vista ocupe todo el espacio de su contenedor padre.
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        // Asignamos nuestro `cameraController` a la `PreviewView`.
        // Esto le dice a la vista qué controlador debe usar para obtener el stream de la cámara.
        previewView.controller = cameraController

        // La `factory` debe devolver la instancia de la Vista creada.
        previewView
    })
}
