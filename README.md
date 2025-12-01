# Captura de Fotos con Jetpack Compose y CameraX

Este es un proyecto de ejemplo para Android que demuestra cómo construir una aplicación de cámara moderna utilizando Jetpack Compose para la interfaz de usuario y la biblioteca CameraX para la funcionalidad de la cámara. La aplicación permite al usuario previsualizar la cámara y capturar una foto con un solo botón.

## Funcionalidades

- **Previsualización en tiempo real**: Muestra la vista de la cámara en vivo en la pantalla.
- **Captura de imágenes**: Permite tomar una foto con un `FloatingActionButton`.
- **Gestión de Permisos**: Solicita el permiso de `CAMERA` de forma segura utilizando la biblioteca Accompanist Permissions.
- **Almacenamiento de Fotos**: Guarda las imágenes capturadas en el directorio público de `Pictures` del dispositivo.
- **Interfaz 100% Compose**: Toda la aplicación, incluida la vista de la cámara, está integrada en una estructura de Jetpack Compose.

## Arquitectura y Explicación del Código

Toda la lógica reside en `MainActivity.kt`, estructurada en varios Composables para una mejor legibilidad y reutilización.

### 1. `CamaraView` (Composable principal)

Es el Composable que orquesta toda la pantalla. Sus responsabilidades son:

- **Solicitar Permisos**: Utiliza `rememberMultiplePermissionsState` de la biblioteca Accompanist para gestionar el permiso `Manifest.permission.CAMERA`.
- **Control de Estado de Permisos**: Un `LaunchedEffect` se encarga de lanzar la petición de permisos en cuanto la pantalla es visible.
- **Inicializar el Controlador**: Crea una instancia de `LifecycleCameraController`, un componente de CameraX que gestiona automáticamente el ciclo de vida de la cámara, evitando la necesidad de abrir y cerrar la cámara manualmente.
- **Diseño con `Scaffold`**: Utiliza un `Scaffold` para estructurar la pantalla, colocando un `FloatingActionButton` en el centro para la acción de captura.
- **Renderizado Condicional**: Muestra la vista de la cámara (`CamaraComposable`) solo si los permisos han sido concedidos. En caso contrario, muestra un texto informativo.

### 2. `CamaraComposable`

Este Composable tiene una única y crucial función: mostrar la previsualización de la cámara.

- **`AndroidView`**: Actúa como un puente para integrar una Vista del sistema tradicional de Android (en este caso, `PreviewView` de CameraX) dentro de la jerarquía de Jetpack Compose.
- **Vinculación del Controlador**: Asigna el `LifecycleCameraController` a la `PreviewView`. Esto permite a CameraX gestionar la superficie de previsualización y mostrar la imagen de la cámara.

### 3. `tomarFoto()` (Función de Captura)

Esta función encapsula la lógica para tomar una foto.

- **Ejecutor**: Utiliza el `mainExecutor` del contexto para asegurar que la respuesta de la captura se procese en el hilo principal.
- **Creación de Archivo**: Genera un archivo `.png` con un nombre único en el directorio público de `Pictures` del dispositivo.
- **`camaraController.takePicture()`**: Es el método de CameraX que inicia la captura. Recibe la configuración del archivo de salida, el ejecutor y un *callback* para manejar el resultado.
- **Callbacks**: El `ImageCapture.OnImageSavedCallback` tiene dos métodos:
    - `onImageSaved`: Se invoca si la foto se guarda con éxito. Imprime la ruta (URI) del archivo guardado.
    - `onError`: Se invoca si ocurre un error durante la captura.

## Librerías y Dependencias Clave

Para que el proyecto funcione, se han añadido las siguientes dependencias en el fichero `build.gradle.kts` del módulo `app`:

- **Jetpack Compose**: Para toda la interfaz de usuario.
  - `activity-compose`, `compose-bom`, `ui`, `ui-graphics`, `material3`, etc.
- **CameraX**: La biblioteca moderna de Android para interactuar con la cámara.
  - `androidx.camera:camera-camera2`: El núcleo que interactúa con la API Camera2 del dispositivo.
  - `androidx.camera:camera-lifecycle`: Proporciona la vinculación automática al ciclo de vida.
  - `androidx.camera:camera-view`: Ofrece componentes de UI como `PreviewView` y `LifecycleCameraController`.
- **Accompanist Permissions**: Una librería de Google (ahora en proceso de integrarse en Jetpack) que simplifica enormemente la solicitud de permisos en tiempo de ejecución dentro de Compose.
  - `com.google.accompanist:accompanist-permissions`

## Permisos Necesarios

La aplicación requiere el siguiente permiso, que debe ser declarado en el archivo `app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

Este permiso es considerado "peligroso" por Android, por lo que no basta con declararlo en el manifiesto. La aplicación debe solicitarlo explícitamente al usuario en tiempo de ejecución, una tarea que es gestionada por la librería `Accompanist Permissions` en el código.