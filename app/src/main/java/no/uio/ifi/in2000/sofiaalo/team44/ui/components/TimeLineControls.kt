package no.uio.ifi.in2000.sofiaalo.team44.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.PlaybackViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TimelineControls(
    modifier: Modifier = Modifier,
    currentIndex: Int,
    timeSteps: List<Long>,
    waypointIndices: List<Int>,
    speedKnots: Double,
    onSpeedChange: (Double) -> Unit,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    onScrub: (Int) -> Unit,
    showSpeed: Boolean = true,
    showWaypoints: Boolean = true,
    showTrajectoryLines: Boolean,
    playbackViewModel: PlaybackViewModel
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd. MMM") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val currentDateTime = remember(currentIndex, timeSteps) {
        timeSteps.getOrNull(currentIndex)?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 0.dp, bottom = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = currentDateTime?.format(dateFormatter) ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = currentDateTime?.format(timeFormatter) ?: "--:--",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.offset(y = (-2).dp)
                )
            }
            Text(
                text = "${currentIndex + 1} / ${timeSteps.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = currentIndex.toFloat(),
                onValueChange = { onScrub(it.toInt()) },
                valueRange = 0f..(timeSteps.size - 1).toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            if (showWaypoints && timeSteps.size > 1 && waypointIndices.isNotEmpty()) {
                // Waypoint rendering can be toggled by caller via provided waypointIndices list.
                WaypointMarkers(waypointIndices, timeSteps.size)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onReset, modifier = Modifier.size(36.dp)) {
                    SizedIcon(Icons.Default.Replay, "Reset", 20.dp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                FilledIconButton(onClick = onPlay, modifier = Modifier.size(44.dp)) {
                    SizedIcon(Icons.Default.PlayArrow, "Play", 28.dp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onStop, modifier = Modifier.size(36.dp)) {
                    SizedIcon(Icons.Default.Pause, "Pause", 24.dp)
                }
            }

            if (showSpeed) {
                Column(
                    modifier = Modifier.width(120.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${speedKnots.toInt()} knop",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Slider(
                        value = speedKnots.toFloat(),
                        onValueChange = { onSpeedChange(it.toDouble()) },
                        valueRange = 1f..30f,
                        modifier = Modifier.height(24.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.width(120.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FilterChip(
                        modifier = Modifier.height(24.dp),
                        selected = showTrajectoryLines,
                        onClick = { playbackViewModel.toggleTrajectoryLines() },
                        label = { Text("Vis baner", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WaypointMarkers(waypointIndices: List<Int>, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .offset(y = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var lastFraction = 0f
        waypointIndices.forEach { index ->
            val fraction = (index.toFloat() / (totalSteps - 1)).coerceIn(0f, 1f)
            val spacerWeight = (fraction - lastFraction).coerceAtLeast(0.0001f)
            Spacer(modifier = Modifier.weight(spacerWeight))
            Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            lastFraction = fraction
        }
        val remainingWeight = (1f - lastFraction).coerceAtLeast(0.0001f)
        Spacer(modifier = Modifier.weight(remainingWeight))
    }
}
@Composable
fun SizedIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    size: androidx.compose.ui.unit.Dp
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(size)
    )
}