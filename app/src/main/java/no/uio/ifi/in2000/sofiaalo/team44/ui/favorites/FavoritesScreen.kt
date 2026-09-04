package no.uio.ifi.in2000.sofiaalo.team44.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.uio.ifi.in2000.sofiaalo.team44.ui.components.FavoriteSimulation
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.PlaybackViewModel

@Composable
fun FavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    playbackViewModel: PlaybackViewModel
) {
    val saved by favoritesViewModel.saved.collectAsStateWithLifecycle()
    val selectedIds by playbackViewModel.selectedIds.collectAsStateWithLifecycle()
    val sortedItems = remember(saved, selectedIds) {
        saved.sortedByDescending { it.uuid in selectedIds }
    }
    var capacityMessage by remember { mutableStateOf<String?>(null) }
    val maxTotalHours by playbackViewModel.maxTotalHours.collectAsStateWithLifecycle()
    val usedHours by playbackViewModel.usedHours.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        favoritesViewModel.loadFavorites()
        playbackViewModel.capacityMessage.collect { message ->
            capacityMessage = message
        }
    }

    Column (modifier = Modifier.fillMaxSize()){
        Text(
            text = "Simulerte isfjell",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.CenterHorizontally)

        )

        Text(
            "$usedHours / $maxTotalHours timer aktive simuleringer",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (capacityMessage != null) {
            Text(
                text = capacityMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        if (saved.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Ingen lagrede simuleringer", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {

            FavoriteSimulation(
                modifier = Modifier.weight(1f),
                items = sortedItems,
                onDelete = { sim ->
                    if (sim.uuid in selectedIds) playbackViewModel.toggleIceberg(sim.uuid)
                    favoritesViewModel.deleteSimulation(sim)
                    playbackViewModel.refresh()
                },
                selectedIds = selectedIds,
                onToggle = { id -> playbackViewModel.toggleIceberg(id) }
            )
        }
    }
}