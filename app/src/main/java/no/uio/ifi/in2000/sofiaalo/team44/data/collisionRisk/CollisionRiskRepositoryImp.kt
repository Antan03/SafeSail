package no.uio.ifi.in2000.sofiaalo.team44.data.collisionRisk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.IcebergTrajectoryData
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.SavedSimulationUi
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.ShipRoute
import no.uio.ifi.in2000.sofiaalo.team44.util.CollisionRiskManager


class CollisionRiskRepositoryImp(
    private val riskManager: CollisionRiskManager = CollisionRiskManager()
) : CollisionRiskRepository {
    override suspend fun calculateRisks(
        activeIcebergs: List<SavedSimulationUi>,
        trajectories: Map<String, IcebergTrajectoryData>,
        shipRoute: ShipRoute,
        thresholdKm: Double
    ): Map<String, Double> = withContext(Dispatchers.Default) {
        riskManager.calculateRisks(activeIcebergs,trajectories,shipRoute, thresholdKm)
    }
}