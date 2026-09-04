package no.uio.ifi.in2000.sofiaalo.team44.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.uio.ifi.in2000.sofiaalo.team44.ui.home.HomeViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.PlaybackViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.SliderMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun RouteInfoBottomSheet(
    currentIndex: Int,
    playbackViewModel: PlaybackViewModel,
    homeViewModel: HomeViewModel
) {
    val route by homeViewModel.route.collectAsStateWithLifecycle()
    val speedKnots by homeViewModel.speedKnots.collectAsStateWithLifecycle()
    val timeSteps by homeViewModel.timeSteps.collectAsStateWithLifecycle()
    val waypointIndices = remember(route, timeSteps) {
        route.waypointIndices()
    }

    val sliderMode by playbackViewModel.sliderMode.collectAsStateWithLifecycle()
    val activeIcebergs by playbackViewModel.activeIcebergs.collectAsStateWithLifecycle()
    val trajectories by playbackViewModel.trajectories.collectAsStateWithLifecycle()
    val showTrajectoryLines by playbackViewModel.showTrajectoryLines.collectAsStateWithLifecycle()

    LaunchedEffect(route) {
        playbackViewModel.updateRoute(route)
    }

    val effectiveTimeSteps = remember(sliderMode, timeSteps, activeIcebergs, trajectories) {
        when (sliderMode) {
            SliderMode.SHIP_ROUTE -> timeSteps

            SliderMode.SIMULATION -> {
                val longest = activeIcebergs.maxByOrNull { traj ->
                    val uuid = traj.uuid
                    trajectories[uuid]?.time?.size ?: 0
                }
                if (longest != null) {
                    val traj = trajectories[longest.uuid]
                    if (traj != null) {
                        val startMs = runCatching {
                            Instant.parse(longest.response.spec.geo.point.time).toEpochMilli()
                        }.getOrElse { return@remember timeSteps }
                        return@remember List(traj.time.size) { i -> startMs + (i * 3_600_000L) }
                    }
                }
                listOf(System.currentTimeMillis())
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .navigationBarsPadding()
            .fillMaxHeight(0.3f)
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets(0, 0, 0, 0)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chipModifier = Modifier.height(24.dp) // Tvinger chipen til å bli lavere
            FilterChip(
                modifier = chipModifier,
                selected = sliderMode == SliderMode.SHIP_ROUTE,
                onClick = { playbackViewModel.setMode(SliderMode.SHIP_ROUTE) },
                label = { Text("Følg rute", style = MaterialTheme.typography.labelSmall) })
            FilterChip(
                modifier = chipModifier,
                selected = sliderMode == SliderMode.SIMULATION,
                onClick = { playbackViewModel.setMode(SliderMode.SIMULATION) },
                label = { Text("Simulering", style = MaterialTheme.typography.labelSmall) })
        }

        if (sliderMode == SliderMode.SIMULATION) {
            if (activeIcebergs.isEmpty()) {
                Text(
                    "Velg en simulering fra listen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                TimelineControls(
                    currentIndex = currentIndex,
                    timeSteps = effectiveTimeSteps,
                    waypointIndices = emptyList(),
                    speedKnots = speedKnots,
                    onSpeedChange = { /* ignored in simulation mode */ },
                    onPlay = { playbackViewModel.startAnimation(effectiveTimeSteps.size - 1) },
                    onStop = { playbackViewModel.stopAnimation() },
                    onReset = { playbackViewModel.resetAnimation() },
                    onScrub = { playbackViewModel.seekTo(it) },
                    showSpeed = false,
                    showWaypoints = false,
                    showTrajectoryLines = showTrajectoryLines,
                    playbackViewModel = playbackViewModel
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Aktive simuleringer", style = MaterialTheme.typography.titleSmall)
            activeIcebergs.forEach { iceberg ->
                val traj = trajectories[iceberg.uuid]
                val steps = traj?.time?.size ?: 0
                val safeIndex = currentIndex.coerceAtMost((steps - 1).coerceAtLeast(0))
                val currentSimTime = runCatching {
                    Instant.parse(iceberg.response.spec.geo.point.time)
                        .plus(safeIndex.toLong(), ChronoUnit.HOURS)
                }.getOrNull()

                val formattedTime = currentSimTime?.let {
                    DateTimeFormatter.ofPattern("dd.MM.yyyy  HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .format(it)
                } ?: "N/A"
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text(
                        text = "ID: ${iceberg.uuid}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Steps: $steps",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Start: $formattedTime   ",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }
                }
            }
        } else {
            TimelineControls(
                currentIndex = currentIndex,
                timeSteps = timeSteps,
                waypointIndices = waypointIndices,
                speedKnots = speedKnots,
                onSpeedChange = { homeViewModel.setSpeed(it) },
                onPlay = { playbackViewModel.startAnimation(effectiveTimeSteps.size - 1) },
                onStop = { playbackViewModel.stopAnimation() },
                onReset = { playbackViewModel.resetAnimation() },
                onScrub = { playbackViewModel.seekTo(it) },
                showTrajectoryLines = showTrajectoryLines,
                playbackViewModel = playbackViewModel
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Ruteoversikt", style = MaterialTheme.typography.titleSmall)

            val routeHours = route.totalDurationHours.toInt()
            Text("Total distanse: ${route.totalDistanceNm.toInt()} nm")
            Text("Estimert tid: $routeHours timer")

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (route.waypoints.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Startposisjon", style = MaterialTheme.typography.labelMedium)

                    Button(
                        onClick = { homeViewModel.removeWaypoint(route.waypoints[0]) },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("Fjern")
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            route.legs.forEachIndexed { i, leg ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Etappe ${i + 1}", style = MaterialTheme.typography.labelMedium)
                    Text("${leg.distanceNm.toInt()} nm  •  ${leg.durationHours.toInt()}t")

                    Button(
                        onClick = { homeViewModel.removeWaypoint(leg.to) },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("Fjern")
                    }
                }
            }
        }
    }
}