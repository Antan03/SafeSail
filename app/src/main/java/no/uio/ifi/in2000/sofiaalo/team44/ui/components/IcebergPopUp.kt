package no.uio.ifi.in2000.sofiaalo.team44.ui.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.Configuration
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.DriftyIceBergSimulationRequest
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.Geo
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.Point
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.Seed
import no.uio.ifi.in2000.sofiaalo.team44.ui.drifty.DriftyViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.favorites.FavoritesViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.home.HomeViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.home.UiState
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.PlaybackViewModel
import no.uio.ifi.in2000.sofiaalo.team44.util.roundTwo


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IcebergPopUp(homeViewModel: HomeViewModel,
                 driftyViewModel: DriftyViewModel,
                 favoritesViewModel: FavoritesViewModel,
                 playbackViewModel: PlaybackViewModel
){
    // henter state fra hjemskjerm
    val icebergState by homeViewModel.icebergUiState.collectAsState()

    val savedSimulations by favoritesViewModel.saved.collectAsState()

    val driftyUiState by driftyViewModel.uiState.collectAsStateWithLifecycle()
    var hoursInput by remember { mutableStateOf("12") }

    // det her er for å vise sheeten (lol) bare når det er valgt et isfjell
    val selectedIceberg = (icebergState as? UiState.Success)?.data?.selectedIceberg
    if (selectedIceberg != null){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        )
        {
            Surface(modifier = Modifier.padding(16.dp)
                .fillMaxWidth()
                .padding(bottom = 80.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
                ) {
                Column(modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Isfjell Info", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))


                    // pos og areal
                Text("Posisjon: lat = ${roundTwo(selectedIceberg.geometry.coordinates[1])}, lon = ${roundTwo(selectedIceberg.geometry.coordinates[0])}")
                Text("Areal: ${ roundTwo(selectedIceberg.properties.BRGARE) } kvm")
                    Text("sist observert: ${selectedIceberg.`when`.instant.subSequence(0, 10)}")
                    OutlinedTextField(
                        value = hoursInput,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                hoursInput = newValue
                            }
                        },
                        label = { Text("Simuleringstid (timer)") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    val simulationHours = hoursInput.toIntOrNull() ?: 0
                    val isOverLimit = simulationHours > 120
                    if (isOverLimit) {
                        Text("Maks 120 timer", color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    }

                    val request = remember(selectedIceberg,simulationHours) {
                        val seed = Seed(
                            draft = 90.0,
                            length = selectedIceberg.properties.IA_BLN,
                            sail = 10.0,
                            width = selectedIceberg.properties.IA_BLN * 0.7
                        )
                        val point = Point(
                            latitude = selectedIceberg.geometry.coordinates[1],
                            longitude = selectedIceberg.geometry.coordinates[0],
                            radiusInMeters = 1000,
                            time = selectedIceberg.`when`.instant
                        )
                        DriftyIceBergSimulationRequest(
                            configuration = Configuration(seed),
                            geo = Geo(point),
                            model = "openberg",
                            simulationDurationInHours = simulationHours
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val checkDuplicates = driftyViewModel.generateTestStableId(request)
                    val alreadyExists = savedSimulations.any { it.uuid == checkDuplicates }
                    if (alreadyExists) {
                        Text(
                            "Simulering allerede lagret for dette isfjellет.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )                    } else {
                        DriftyStartSimulering(
                            driftyUiState = driftyUiState,
                            onStart = {
                                driftyViewModel.startSimulation(request)
                            },
                            simulationHours = simulationHours,
                            driftyViewModel = driftyViewModel,
                            favoritesViewModel = favoritesViewModel,
                            playbackViewModel = playbackViewModel
                        )
                    }
                Spacer(modifier = Modifier.height(32.dp))
                    TextButton(onClick = {
                        homeViewModel.clearSelect()
                        driftyViewModel.resetSimulationStatus()
                    }) {
                        Text("Lukk")
                    }
            }
        }
    }
}}