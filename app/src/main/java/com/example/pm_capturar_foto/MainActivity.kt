package com.example.pm_capturar_foto

import android.Manifest
import android.R.attr.contentDescription

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pm_capturar_fotoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CamaraView()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CamaraView() {
    val permissions = rememberMultiplePermissionsState(
        //permissions = listOf(
            //Manifest.permission.CAMERA,
            ///Manifest.permission.READ_EXTERNAL_STORAGE,
            ///Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions = listOf(
            Manifest.permission.CAMERA
        )
    )

    val context = LocalContext.current
    val camaraController = remember{ LifecycleCameraController(context) }
    val lifecycle = LocalLifecycleOwner.current

    val directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absoluteFile

    LaunchedEffect(key1=Unit) {
        permissions.launchMultiplePermissionRequest()
    }


    Scaffold(modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val executor = ContextCompat.getMainExecutor(context)
                    tomarFoto(camaraController,executor, directorio)
                }
            )
            {
                Icon(
                    painterResource(id=R.drawable.icon_camera),
                    tint = Color.White,
                    contentDescription = null
                )

            }


        },
        floatingActionButtonPosition = FabPosition.Center
    )
    {
        if (permissions.allPermissionsGranted) {
            CamaraComposable(
                camaraController = camaraController,
                lifecycleOwner = lifecycle,
                modifier = Modifier.padding(it)
            )
        } else {
            Text(text = "Permisos no concedidos", modifier= Modifier.padding(it))
        }
    }

}


@Composable
fun CamaraComposable(
    camaraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier
) {
    camaraController.bindToLifecycle(lifecycleOwner  )
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


private fun tomarFoto(
    camaraController: LifecycleCameraController,
    executor: Executor,
    directorio: File
) {
    val image= File.createTempFile("img_", ".png", directorio)
    val outputDirectory = ImageCapture.OutputFileOptions.Builder(image).build()
    camaraController.takePicture(
        outputDirectory,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                println("foto_tomada")
                println(outputFileResults.savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                println()
            }


        }
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Pm_capturar_fotoTheme {
        CamaraView()
    }
}