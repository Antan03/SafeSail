package no.uio.ifi.in2000.sofiaalo.team44.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.sofiaalo.team44.data.simulationStorage.SimulationStorageRepository
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.SavedSimulationUi

class FavoritesViewModel(
    private val simulationStorageRepository: SimulationStorageRepository
) : ViewModel() {
    private val _saved = MutableStateFlow<List<SavedSimulationUi>>(emptyList())
    val saved: StateFlow<List<SavedSimulationUi>> = _saved

    fun loadFavorites() {
        viewModelScope.launch {
            _saved.value = simulationStorageRepository.loadSavedSimulations()
        }
    }

    fun deleteSimulation(sim: SavedSimulationUi) {
        viewModelScope.launch {
            sim.ncFile.delete()
            sim.jsonFile.delete()
            loadFavorites()
        }
    }
}
