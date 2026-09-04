package no.uio.ifi.in2000.sofiaalo.team44.data.collisionRisk

import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.IcebergTrajectoryData
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.SavedSimulationUi
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.ShipRoute



interface CollisionRiskRepository {
    suspend fun calculateRisks(
        activeIcebergs: List<SavedSimulationUi>,
        trajectories: Map<String, IcebergTrajectoryData>,
        shipRoute: ShipRoute,
        thresholdKm: Double = 50.0
    ): Map<String, Double>
}
