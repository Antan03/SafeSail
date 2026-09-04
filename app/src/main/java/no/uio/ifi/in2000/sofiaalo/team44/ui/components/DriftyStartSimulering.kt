package no.uio.ifi.in2000.sofiaalo.team44.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.sofiaalo.team44.data.drifty.SimulationStatus
import no.uio.ifi.in2000.sofiaalo.team44.ui.drifty.DriftyViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.drifty.UiState
import no.uio.ifi.in2000.sofiaalo.team44.ui.favorites.FavoritesViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.PlaybackViewModel
import kotlin.math.abs

    @Composable
    fun DriftyStartSimulering(
        driftyUiState: UiState,
        onStart: () -> Unit,
        simulationHours: Int,
        driftyViewModel: DriftyViewModel,
        favoritesViewModel: FavoritesViewModel,
        playbackViewModel: PlaybackViewModel
        ) {
        val currentStatus by driftyViewModel.simulationState.collectAsState()
        LaunchedEffect(currentStatus) {
            if (currentStatus is SimulationStatus.Success) {
                favoritesViewModel.loadFavorites()
                playbackViewModel.refresh()
                delay(3000L)
                driftyViewModel.resetSimulationStatus()
            }
        }
        Column {

            when (val state = currentStatus) {

                is SimulationStatus.Starting -> {
                    Text("Kontakter server...")
                }

                is SimulationStatus.Waiting -> {
                    val estimatedTime = 60f + (simulationHours * 0.65f)
                    val progress = (state.seconds / estimatedTime).coerceAtMost(0.99f)

                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.size(16.dp))
                        LinearProgressIndicator(
                            progress = {animatedProgress},
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )

                        Text("${(progress * 100).toInt()}% ferdig")

                        val remaining = estimatedTime - state.seconds

                        if (remaining > 0) {
                            val secondsLeft = remaining.toInt()
                            Text(
                                "Ca. $secondsLeft sekunder igjen",
                                style = MaterialTheme.typography.labelSmall
                            )
                        } else {
                            val overtime = abs(remaining).toInt()
                            Text(
                                "Serveren bruker lenger tid enn forventet... (+${overtime}s)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                is SimulationStatus.Error -> {
                    Text("Noe gikk galt: ${state.message}", color = Color.Red)
                    Button(onClick = onStart) { Text("Prøv igjen") }
                }

                is SimulationStatus.Success -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(progress = {1f}, color = Color.Green)
                        Text("100% - Simulering fullført!")
                        Text("Tid brukt: ${state.totalSecondsElapsed} sek")
                    }
                }

                is SimulationStatus.Idle -> {
                    if (driftyUiState is UiState.Idle) {
                        Button(onClick = onStart) {
                            Text("Start isfjell-simulering")
                        }

                    }
                }
            }
            if (driftyUiState is UiState.Error && currentStatus is SimulationStatus.Idle) {
                Text("Feil: ${driftyUiState.message}", color = Color.Red)
            }
        }
    }
