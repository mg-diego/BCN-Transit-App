import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.model.transport.NearbyStation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    initialQuery: String = "",
    onSearch: (String, String, String) -> Unit,
    enabled: Boolean
) {
    var query by remember { mutableStateOf(initialQuery) }
    var active by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var noResults by remember { mutableStateOf(false) }
    var stations by remember { mutableStateOf<List<NearbyStation>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    val suggestions = remember(query, stations) { stations.take(20) }

    LaunchedEffect(Unit) {
        snapshotFlow { query }
            .debounce(300)
            .collectLatest { q ->
                if (q.isNotBlank() && q.length >= 3) {
                    isSearching = true
                    stations = ApiClient.resultsApiService.getResultsByName(q)
                    noResults = stations.isEmpty()
                    isSearching = false
                } else {
                    stations = emptyList()
                    noResults = false
                    isSearching = false
                }
            }
    }

    if(enabled) {
        SearchBar(
            query = query,
            onQueryChange = { if (enabled) query = it },
            onSearch = {},
            active = active,
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            onActiveChange = {
                if (enabled) {
                    active = it
                    if (!it) {
                        query = ""
                    }
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
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color.White
                    )
                }
            },
            placeholder = { Text("Buscar estación o dirección") },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!active) Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    else Modifier
                )
        ) {
            if (isSearching) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }
            } else {
                if (stations.isEmpty()) {
                    if (noResults) {
                        Text(
                            text = "No se encontraron coincidencias. Prueba con otro nombre o con el identificador numérico.",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Text(
                            text = "Puedes escribir el nombre completo o parcial del elemento que buscas, o introducir su identificador numérico si lo conoces.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 1200.dp)
                    ) {
                        items(suggestions) { station ->
                            ListItem(
                                headlineContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val context = LocalContext.current
                                        val drawableName = "${station.type}_${
                                            station.line_name?.lowercase()?.replace(" ", "_")
                                        }"
                                        val drawableId = remember(station.line_name) {
                                            context.resources.getIdentifier(
                                                drawableName,
                                                "drawable",
                                                context.packageName
                                            ).takeIf { it != 0 }
                                                ?: context.resources.getIdentifier(
                                                    station.type,
                                                    "drawable",
                                                    context.packageName
                                                )
                                        }

                                        Icon(
                                            painter = painterResource(drawableId),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("(${station.station_code}) - ${station.station_name}")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = enabled) {
                                        query = station.station_name
                                        active = false
                                        stations = emptyList()

                                        coroutineScope.launch {
                                            try {
                                                onSearch(station.type, station.line_code ?: "", station.station_code)
                                            } catch (e: Exception) {
                                                // Manejo de error, mostrar snackbar, log, etc.
                                            }
                                        }
                                    }
                            )
                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                DividerDefaults.color
                            )
                        }
                    }
                }
            }
        }
    }
}
