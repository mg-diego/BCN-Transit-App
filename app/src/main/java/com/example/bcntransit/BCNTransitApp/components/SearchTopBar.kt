import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.NorthWest // Flecha para rellenar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bcntransit.app.R
import com.bcntransit.app.api.ApiClient
import com.bcntransit.app.model.transport.NearbyStation
import com.bcntransit.app.util.getAndroidId
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    initialQuery: String = "",
    onSearch: (String, String, String) -> Unit,
    enabled: Boolean,
    onActiveChange: (Boolean) -> Unit = {}
) {
    var query by remember { mutableStateOf(initialQuery) }
    var active by remember { mutableStateOf(false) }

    // Resultados de búsqueda (Objetos complejos)
    var isSearching by remember { mutableStateOf(false) }
    var noResults by remember { mutableStateOf(false) }
    var stations by remember { mutableStateOf<List<NearbyStation>>(emptyList()) }

    // Historial (Strings simples) -> CAMBIO DE TIPO AQUÍ
    var searchHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingHistory by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val suggestions = remember(query, stations) { stations.take(20) }
    val currentUserId = getAndroidId(LocalContext.current)

    // 1. Búsqueda en tiempo real (Sigue igual)
    LaunchedEffect(Unit) {
        snapshotFlow { query }
            .debounce(300)
            .collectLatest { q ->
                if (q.isNotBlank() && q.length >= 3) {
                    isSearching = true
                    // Asumo que esta llamada devuelve List<NearbyStation>
                    stations = ApiClient.resultsApiService.getResultsByName(q, currentUserId)
                    noResults = stations.isEmpty()
                    isSearching = false
                } else {
                    stations = emptyList()
                    noResults = false
                    isSearching = false
                }
            }
    }

    // 2. Cargar historial (Strings)
    LaunchedEffect(active) {
        if (active && query.isBlank()) {
            isLoadingHistory = true
            try {
                val rawHistory = ApiClient.resultsApiService.getSearchHistory(currentUserId)
                searchHistory = rawHistory
            } catch (e: Exception) {
                searchHistory = emptyList()
            } finally {
                isLoadingHistory = false
            }
        }
    }

    if(enabled) {
        SearchBar(
            query = query,
            onQueryChange = { if (enabled) query = it },
            onSearch = {}, // El enter del teclado podría disparar algo aquí si quisieras
            active = active,
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            onActiveChange = { isActive ->
                if (enabled) {
                    active = isActive
                    onActiveChange(isActive)
                    if (!isActive) query = ""
                }
            },
            enabled = enabled,
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(colorResource(R.color.red), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (query.isEmpty() && active) Icons.Default.History else Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            placeholder = {
                Text("Buscar estación...")
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!active) Modifier.padding(horizontal = 16.dp, vertical = 8.dp) else Modifier)
        ) {

            if (isSearching) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }
            }
            else if (query.isNotBlank()) {
                // --- MODO RESULTADOS (Objetos NearbyStation) ---
                if (stations.isEmpty()) {
                    if (noResults) {
                        Text("No se encontraron coincidencias.", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    ResultsList(
                        items = suggestions,
                        enabled = enabled,
                        onItemClick = { station ->
                            query = station.station_name
                            active = false
                            onActiveChange(false)
                            stations = emptyList()
                            coroutineScope.launch {
                                try {
                                    onSearch(station.type, station.line_code ?: "", station.station_code)
                                } catch (e: Exception) { }
                            }
                        }
                    )
                }
            }
            else {
                // --- MODO HISTORIAL (Strings) ---
                if (isLoadingHistory) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Gray)
                    }
                } else if (searchHistory.isNotEmpty()) {
                    Column {
                        SimpleHistoryList(
                            historyItems = searchHistory,
                            onHistoryClick = { historyText ->
                                query = historyText
                            }
                        )
                    }
                } else {
                    Text(
                        text = "Puedes escribir el nombre completo o parcial del elemento que buscas, o introducir su identificador numérico si lo conoces.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// --- NUEVO COMPONENTE PARA PINTAR STRINGS ---
@Composable
fun SimpleHistoryList(
    historyItems: List<String>,
    onHistoryClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 1200.dp)
    ) {
        items(historyItems) { text ->
            ListItem(
                headlineContent = {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.NorthWest,
                        contentDescription = "Usar",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHistoryClick(text) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun ResultsList(
    items: List<NearbyStation>,
    enabled: Boolean,
    onItemClick: (NearbyStation) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 1200.dp)) {
        items(items) { station ->
            ListItem(
                headlineContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val context = LocalContext.current
                        val drawableName = "${station.type}_${station.line_name?.lowercase()?.replace(" ", "_")}"
                        val drawableId = remember(station.line_name) {
                            context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                                .takeIf { it != 0 }
                                ?: context.resources.getIdentifier(station.type, "drawable", context.packageName)
                        }

                        Icon(
                            painter = painterResource(drawableId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = station.station_name, style = MaterialTheme.typography.bodyLarge)
                            Text(text = "(${station.station_code})", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onItemClick(station) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = DividerDefaults.Thickness, color = DividerDefaults.color.copy(alpha = 0.5f))
        }
    }
}