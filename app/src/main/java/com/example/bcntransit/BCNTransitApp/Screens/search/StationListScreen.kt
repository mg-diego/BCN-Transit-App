package com.example.bcntransit.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bcntransit.data.enums.CustomColors
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.StationDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListScreen(
    line: LineDto,
    stations: List<StationDto>,
    loading: Boolean,
    error: String? = null,
    onStationClick: (StationDto) -> Unit
) {
    val parsedColor = Color(
        android.graphics.Color.parseColor(
            if (line.color.startsWith("#")) line.color else "#${line.color}"
        )
    )
    val context = LocalContext.current
    val drawableName = "${line.transport_type}_${line.name.lowercase().replace(" ", "_")}"
    val drawableId = remember(line.name) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }

    // Valores dinámicos de origen/destino
    val origen = "${line.origin} → ${line.destination}"
    val destino = "${line.destination} → ${line.origin}"

    // Estado del selector: por defecto vacío, se fijará a DESTI cuando haya datos
    var direction by remember { mutableStateOf("") }

    // Cuando cambien las estaciones, establecemos DESTI_SERVEI como seleccionado
    LaunchedEffect(destino) {
        if (destino.isNotEmpty()) {
            direction = origen
        }
    }

    Column {

        // Cabecera con sombra
        Box(Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(drawableId),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(line.description, style = MaterialTheme.typography.titleLarge)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // === Toggle Segmentado ancho ===
        if (origen != " → " && destino != " → ") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SegmentedSelector(
                    options = listOf(origen, destino),
                    direction = direction,
                    onOptiondirection = { direction = it },
                    modifier = Modifier.fillMaxWidth(0.9f), // ocupa ~90% del ancho
                    lineColor = parsedColor
                )
            }
        }

        when {
            loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            error != null -> Text("Error: $error", color = Color.Red)

            else -> LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                var showStations: List<StationDto>? = null
                if (line.transport_type == "bus") {
                    showStations = if (direction == origen) stations.filter { it.DESTI_SENTIT == line.destination } else stations.filter { it.DESTI_SENTIT == line.origin }
                } else {
                    showStations = if (direction == origen) stations else stations.reversed()
                }

                itemsIndexed(showStations) { index, station ->
                    StationRow(
                        station = station,
                        isFirst = index == 0,
                        isLast = index == stations.lastIndex,
                        lineColor = parsedColor,
                        onClick = { onStationClick(station) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentedSelector(
    options: List<String>,
    direction: String,
    onOptiondirection: (String) -> Unit,
    modifier: Modifier = Modifier,
    lineColor: Color
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        options.forEachIndexed { index, label ->
            val isdirection = direction == label

            Surface(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                color = if (isdirection) lineColor else lineColor.copy(alpha = 0x0F / 255f),
                contentColor = if (isdirection) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f) // ocupa el mismo espacio cada opción
                    .padding(horizontal = 2.dp) // pequeño espacio entre botones
                    .clickable { onOptiondirection(label) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isdirection) androidx.compose.ui.text.font.FontWeight.Bold
                            else androidx.compose.ui.text.font.FontWeight.Normal
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(3.dp)
                    )
                }
            }
        }
    }
}

