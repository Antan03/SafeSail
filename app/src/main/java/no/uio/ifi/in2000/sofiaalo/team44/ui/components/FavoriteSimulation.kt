package no.uio.ifi.in2000.sofiaalo.team44.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.SavedSimulationUi

@Composable
fun FavoriteSimulation(
    modifier: Modifier,
    items: List<SavedSimulationUi>,
    onDelete: (SavedSimulationUi) -> Unit,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit
) {
    val selected = remember(items, selectedIds) { items.filter { it.uuid in selectedIds } }
    val unselected = remember(items, selectedIds) { items.filter { it.uuid !in selectedIds } }

    Column(modifier = modifier) {

        if (selected.isNotEmpty()) {
            Text(
                "${selected.size} aktive simuleringer",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyColumn(modifier = Modifier.weight(0.3f)) {
                items(selected) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clickable { onToggle(item.uuid) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "Aktiv simulering",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "ID: ${item.uuid.take(8)}",
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                            IconButton(
                                onClick = { onDelete(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    tint = Color.Red,
                                    contentDescription = "Slett simulering",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        LazyColumn(modifier = Modifier.weight(0.7f) ) {
            items(unselected) { item ->
                val response = item.response
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onToggle(item.uuid) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = response.spec.geo.point.time,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { onDelete(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Slett", tint = Color.Red)
                            }
                        }

                        val point = response.spec.geo.point
                        val seed = response.spec.configuration.seed

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("ID: ${item.uuid}", style = MaterialTheme.typography.bodyMedium)
                        Text("lat: ${"%.4f".format(point.latitude)}, lon: ${"%.4f".format(point.longitude)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Varighet: ${response.spec.simulationDurationInHours} timer", style = MaterialTheme.typography.bodyMedium)
                        Text("Dimensjoner: ${seed.length}m x ${seed.width}m", style = MaterialTheme.typography.bodyMedium)

                        val uriHandler = LocalUriHandler.current
                        Text(
                            text = "Klikk her for mer info (logg)",
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { uriHandler.openUri(response.result.files.log) }
                        )

                        val plotUrl = response.result.files.plot
                        if (plotUrl != null) {
                            AsyncImage(
                                model = plotUrl,
                                contentDescription = "Simuleringsplot",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}