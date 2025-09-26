package com.example.bcntransit

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar SplashScreen
        //val splashScreen = installSplashScreen()

        // Ajustes de ventana y barra de estado
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        super.onCreate(savedInstanceState)

        // Inicializar MapLibre
        MapLibre.getInstance(this)

        // Estado de carga global

        // Mantener splash hasta que se carguen los datos
        //splashScreen.setKeepOnScreenCondition { isLoading.value }

        setContent {
            BCNTransitApp()
        }

        // Solicitar permiso de ubicación
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}