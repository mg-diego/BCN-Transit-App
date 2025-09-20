package com.example.bcntransit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.model.MetroLineDto
import com.example.bcntransit.model.TramLineDto
import com.example.bcntransit.util.UserIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)

        setContent {
            BCNTransitApp()
        }
    }
}

@Composable
fun BCNTransitApp() {
    val context = LocalContext.current

    // Solicitar permiso de ubicación
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    var selectedTab by remember { mutableStateOf(BottomTab.MAP) }
    var currentSearchScreen by remember { mutableStateOf<SearchOption?>(null) }

    // Estado para metro
    var metroLines by remember { mutableStateOf<List<MetroLineDto>>(emptyList()) }
    var tramLines by remember { mutableStateOf<List<TramLineDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Función para cargar líneas de metro
    fun loadMetroLines() {
        loading = true
        error = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lines = ApiClient.metroApiService.getMetroLines()
                metroLines = lines
                loading = false
            } catch (e: Exception) {
                e.printStackTrace()
                error = e.message
                loading = false
            }
        }
    }
    fun loadTramLines() {
        loading = true
        error = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lines = ApiClient.tramApiService.getTramLines()
                tramLines = lines
                loading = false
            } catch (e: Exception) {
                e.printStackTrace()
                error = e.message
                loading = false
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedTab) { tab ->
                selectedTab = tab
                if (tab == BottomTab.SEARCH) {
                    currentSearchScreen = null // reset al entrar en Buscar
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                BottomTab.MAP -> MapScreen(context)
                BottomTab.SEARCH -> {
                    if (currentSearchScreen == null) {
                        SearchScreenModern { currentSearchScreen = it }
                    } else {
                        when (currentSearchScreen) {
                            SearchOption.METRO -> {
                                LaunchedEffect(Unit) { loadMetroLines() }
                                MetroListScreen(metroLines, loading, error)
                            }
                            SearchOption.BUS -> PlaceholderScreen("Bus")
                            SearchOption.TRAM -> {
                                LaunchedEffect(Unit) { loadTramLines() }
                                TramListScreen(tramLines, loading, error)
                            }
                            SearchOption.RODALIES -> PlaceholderScreen("Rodalies")
                            SearchOption.FGC -> PlaceholderScreen("FGC")
                            SearchOption.BICING -> PlaceholderScreen("Bicing")
                            null -> TODO()
                        }
                    }
                }
                BottomTab.FAVORITES -> PlaceholderScreen("Favoritos")
                BottomTab.USER -> {
                    var currentScreen by remember { mutableStateOf<SettingsOption?>(null) }

                    if (currentScreen == null) {
                        SettingsScreenModern { currentScreen = it }
                    } else {
                        when (currentScreen) {
                            SettingsOption.NOTIFICATIONS -> PlaceholderScreen("Notificaciones")
                            SettingsOption.LANGUAGE -> PlaceholderScreen("Idioma")
                            SettingsOption.HELP -> PlaceholderScreen("Ayuda")
                            null -> TODO()
                        }
                    }
                }
            }
        }
    }
}

// --------------------- Bottom Navigation ---------------------
enum class BottomTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MAP("Mapa", Icons.Default.Home),
    SEARCH("Buscar", Icons.Default.Search),
    FAVORITES("Favoritos", Icons.Default.Favorite),
    USER("Usuario", Icons.Default.AccountCircle)
}

@Composable
fun BottomNavigationBar(selectedTab: BottomTab, onTabSelected: (BottomTab) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        BottomTab.values().forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}

// --------------------- Placeholder ---------------------
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title (Placeholder)")
    }
}

// --------------------- SEARCH ---------------------
@Composable
fun SearchScreenModern(onNavigate: (SearchOption) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Buscar", style = MaterialTheme.typography.headlineMedium)

        val searchItems = listOf(
            Triple("Metro", "Ver líneas y estaciones de metro", "https://tmb-barcelona.github.io/TMB-Icons/svg/METRO.svg"),
            Triple("Bus", "Ver líneas y paradas de bus", "https://tmb-barcelona.github.io/TMB-Icons/svg/BUS.svg"),
            Triple("Tram", "Ver líneas y paradas de tram", "https://tmb-barcelona.github.io/TMB-Icons/svg/TRAM.svg"),
            Triple("Rodalies", "Ver líneas y estaciones de Rodalies", "https://tmb-barcelona.github.io/TMB-Icons/svg/RODALIES.svg"),
            Triple("FGC", "Ver líneas y estaciones de FGC", "https://tmb-barcelona.github.io/TMB-Icons/svg/FGC.svg"),
            Triple("Bicing", "Ver estaciones de Bicing", "https://tmb-barcelona.github.io/TMB-Icons/svg/BICING_ESTACIO.svg")
        )

        searchItems.forEachIndexed { index, item ->
            SearchCard(
                iconUrl = item.third,
                title = item.first,
                description = item.second,
                onClick = { onNavigate(SearchOption.values()[index]) }
            )
        }
    }
}

@Composable
fun SearchCard(
    iconUrl: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (iconUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(iconUrl)
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    contentDescription = title,
                    modifier = Modifier.size(36.dp)
                )
            } else if (icon != null) {
                Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(36.dp))
            }

            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MetroListScreen(lines: List<MetroLineDto>, loading: Boolean, error: String?) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Metro", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Text("Error: $error", color = Color.Red)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(lines) { line ->
                    MetroLineCard(line)
                }
            }
        }
    }
}
@Composable
fun TramListScreen(lines: List<TramLineDto>, loading: Boolean, error: String?) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tram", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Text("Error: $error", color = Color.Red)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(lines) { line ->
                    TramLineCard(line)
                }
            }
        }
    }
}

@Composable
fun MetroLineCard(line: MetroLineDto) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { /* navegar a detalle */ },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("https://tmb-barcelona.github.io/TMB-Icons/svg/${line.ORIGINAL_NOM_LINIA}.svg")
                    .decoderFactory(SvgDecoder.Factory())
                    .build(),
                contentDescription = "#${line.ORIGINAL_NOM_LINIA}",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            val alertText = if (line.has_alerts) "Incidencias" else "Servicio normal"
            val alertColor = if (line.has_alerts) Color.Red else Color.Green
            Column {
                Text(line.DESC_LINIA, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Circulo de color
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(alertColor, shape = CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Texto de alerta
                    Text(
                        text = alertText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
@Composable
fun TramLineCard(line: TramLineDto) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { /* navegar a detalle */ },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("https://tmb-barcelona.github.io/TMB-Icons/svg/${line.original_name}.svg")
                    .decoderFactory(SvgDecoder.Factory())
                    .build(),
                contentDescription = "#${line.original_name}",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            val alertText = if (line.has_alerts) "Incidencias" else "Servicio normal"
            val alertColor = if (line.has_alerts) Color.Red else Color.Green
            Column {
                Text(line.original_name.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Circulo de color
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(alertColor, shape = CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Texto de alerta
                    Text(
                        text = alertText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --------------------- SETTINGS ---------------------
@Composable
fun SettingsScreenModern(onNavigate: (SettingsOption) -> Unit) {
    val userId = UserIdentifier.getUserId(LocalContext.current)
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Usuario", style = MaterialTheme.typography.headlineMedium)

        SettingsCard(Icons.Default.Notifications, "Notificaciones", "Configura tus alertas y notificaciones") { onNavigate(SettingsOption.NOTIFICATIONS) }
        SettingsCard(Icons.Default.Face, "Idioma", "Selecciona el idioma de la app") { onNavigate(SettingsOption.LANGUAGE) }
        SettingsCard(Icons.Default.Build, "Ayuda", "Consulta la guía de uso") { onNavigate(SettingsOption.HELP) }
    }
}

@Composable
fun SettingsCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(36.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

enum class SettingsOption { NOTIFICATIONS, LANGUAGE, HELP }
enum class SearchOption { METRO, BUS, TRAM, RODALIES, FGC, BICING }

// --------------------- MAP ---------------------
@Composable
fun MapScreen(context: android.content.Context) {
    val mapView = rememberMapView(context)
    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}

fun hasLocationPermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun rememberMapView(context: android.content.Context): MapView {
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                map.setStyle(Style.Builder().fromUri("https://basemaps.cartocdn.com/gl/positron-gl-style/style.json")) { style ->
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(41.3874, 2.1686))
                        .zoom(14.0)
                        .build()

                    if (hasLocationPermission(context)) {
                        val locationComponent = map.locationComponent
                        val options = LocationComponentActivationOptions.builder(context, style).useDefaultLocationEngine(true).build()
                        locationComponent.activateLocationComponent(options)
                        locationComponent.isLocationComponentEnabled = true
                        locationComponent.cameraMode = CameraMode.TRACKING
                        locationComponent.renderMode = RenderMode.COMPASS
                    }
                }
            }
        }
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    return mapView
}
