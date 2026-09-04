package no.uio.ifi.in2000.sofiaalo.team44.ui.drifty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.sofiaalo.team44.data.drifty.DriftyRepository
import no.uio.ifi.in2000.sofiaalo.team44.data.drifty.SimulationStatus
import no.uio.ifi.in2000.sofiaalo.team44.data.simulationStorage.SimulationStorageRepository
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.DriftyIceBergSimulationRequest

class DriftyViewModel(
    private val simulationRepository: DriftyRepository,
    private val storageRepository: SimulationStorageRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private val _simulationState =
        MutableStateFlow<SimulationStatus>(SimulationStatus.Idle)
    val simulationState: StateFlow<SimulationStatus> =
        _simulationState.asStateFlow()

    fun startSimulation(request: DriftyIceBergSimulationRequest) {
        viewModelScope.launch {

            simulationRepository.getSimulationResult(request).collect { status ->
                _simulationState.value = status

                when (status) {

                    is SimulationStatus.Success -> {

                        val response = status.data
                        val url = response.result.files.main ?: return@collect

                        val file = storageRepository.downloadNetCDF(
                            url = url,
                            response = response
                        )

                        if (file == null) {
                            return@collect
                        }

                        _uiState.value = UiState.Success(response)
                    }

                    is SimulationStatus.Error -> {
                        _uiState.value = UiState.Error()
                    }
                    else -> Unit
                }
            }
        }
    }
    //nulstilelr state etter simulasjonen er kjørt
    fun resetSimulationStatus() {
        _simulationState.value = SimulationStatus.Idle
        _uiState.value = UiState.Idle
    }
    fun generateTestStableId(spec: DriftyIceBergSimulationRequest): String {
        val config = spec.configuration.seed
        val point = spec.geo.point

        val rawString =
            "${"%.4f".format(point.latitude)}${"%.4f".format(point.longitude)}${point.time}${point.radiusInMeters}" +
                    "${config.width}${config.length}${config.draft}${config.sail}" +
                    "${spec.simulationDurationInHours}${spec.model}"
        return rawString.hashCode().toString()
    }
}
