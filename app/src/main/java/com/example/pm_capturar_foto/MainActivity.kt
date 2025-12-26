package com.example.pm_capturar_foto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.pm_capturar_foto.home.HomeScreen
import com.example.pm_capturar_foto.ui.theme.Pm_capturar_fotoTheme


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
                    HomeScreen()
                }
            }
        }
    }
}
