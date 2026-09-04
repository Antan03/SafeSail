package no.uio.ifi.in2000.sofiaalo.team44.ui.probability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.sofiaalo.team44.data.collisionRisk.CollisionRiskRepository
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.IcebergTrajectoryData
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.SavedSimulationUi
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.ShipRoute

class ProbabilityViewModel(
    private val riskRepository: CollisionRiskRepository
) : ViewModel() {

    private val _riskResults = MutableStateFlow<Map<String, Double>>(emptyMap())
    val riskResults = _riskResults.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating = _isCalculating.asStateFlow()

    private val _latestRoute = MutableStateFlow(ShipRoute())
    val latestRoute = _latestRoute.asStateFlow()

    private val _thresholdKm = MutableStateFlow(50.0)

    private var latestIcebergs: List<SavedSimulationUi> = emptyList()
    private var latestTrajectories: Map<String, IcebergTrajectoryData> = emptyMap()

    fun setThresholdKm(km: Double) {
        _thresholdKm.value = km
    }

    fun updateData(
        activeIcebergs: List<SavedSimulationUi>,
        trajectories: Map<String, IcebergTrajectoryData>,
        shipRoute: ShipRoute
    ) {
        if (activeIcebergs.isEmpty() || shipRoute.waypoints.size < 2) return
        latestIcebergs = activeIcebergs
        latestTrajectories = trajectories
        _latestRoute.value = shipRoute
        viewModelScope.launch {
            _isCalculating.value = true
            val results = riskRepository.calculateRisks(
                activeIcebergs = latestIcebergs,
                trajectories = latestTrajectories,
                shipRoute = _latestRoute.value,
                thresholdKm = _thresholdKm.value
            )
            _riskResults.value = results
            _isCalculating.value = false
        }
    }
}