import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    modifier: Modifier = Modifier,
    initialQuery: String = "",
    onSearch: (String) -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }
    var active by remember { mutableStateOf(false) }

    SearchBar(
        query = query,
        onQueryChange = { query = it },
        onSearch = {
            onSearch(query)
            active = false
        },
        active = active,
        onActiveChange = { active = it },
        leadingIcon = {
            // Botón circular rojo con la lupa
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

                Spacer(modifier = Modifier.width(16.dp))
            }
        },
        placeholder = { Text("Buscar estación o dirección") },
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Opcional: aquí puedes mostrar resultados sugeridos mientras se escribe
        // Example:
        // Column { Text("Sugerencia 1"); Text("Sugerencia 2") }
    }
}
