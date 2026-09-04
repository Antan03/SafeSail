package no.uio.ifi.in2000.sofiaalo.team44.util

import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.DriftyIcebergSimulationResponse
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.IcebergTrajectoryData
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.SavedSimulationUi
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.LatLon
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.ShipRoute
import java.time.Instant


class CollisionRiskManager {

    fun calculateRisks(
        activeIcebergs: List<SavedSimulationUi>,
        trajectories: Map<String, IcebergTrajectoryData>,
        shipRoute: ShipRoute,
        thresholdKm: Double = 50.0
    ): Map<String, Double> {
        if (shipRoute.waypoints.size < 2 || activeIcebergs.isEmpty()) return emptyMap()

        val shipPositions: Map<Long, LatLon> = buildShipPositionMap(shipRoute)

        return activeIcebergs.associate { iceberg ->
            val risk = calculateCollisionRisk(
                trajectory = trajectories[iceberg.uuid] ?: return@associate iceberg.uuid to 0.0,
                icebergStartMs = parseIcebergStartTime(iceberg.response),
                numTimeSteps = iceberg.response.spec.simulationDurationInHours + 1,
                shipPositions = shipPositions,
                thresholdKm = thresholdKm
            )
            iceberg.uuid to risk
        }
    }
    /**
     * Parser ISO-8601-strengen fra ResponsePoint.time til Unix-millisekunder.
     * Eksempel: "2024-03-15T12:00:00Z" -> 1710504000000L
     */
    private fun parseIcebergStartTime(response: DriftyIcebergSimulationResponse): Long {
        return Instant.parse(response.spec.geo.point.time).toEpochMilli()
    }

    private fun buildShipPositionMap(shipRoute: ShipRoute): Map<Long, LatLon> {
        val interpolated = shipRoute.getInterpolatedRoute(intervalMs = 3_600_000L)
        return interpolated
            .mapIndexed { index, latLon -> (shipRoute.startTime + index * 3_600_000L) to latLon }
            .toMap()
    }

    private fun calculateCollisionRisk(
        trajectory: IcebergTrajectoryData,
        icebergStartMs: Long,
        numTimeSteps: Int,
        shipPositions: Map<Long, LatLon>,
        thresholdKm: Double
    ): Double {
        val numTrajectories = 1000
        val actualSteps = minOf(numTimeSteps, trajectory.lat.size / numTrajectories)
        if (actualSteps == 0) return 0.0

        var maxHitFraction = 0.0

        for (t in 0 until actualSteps) {
            val icebergTimeMs = icebergStartMs + (t * 3_600_000L)
            val roundedTime = (icebergTimeMs / 3_600_000L) * 3_600_000L
            val shipPos = shipPositions[roundedTime] ?: continue
            var hitsThisStep = 0
            val stepStartIdx = t * numTrajectories

            for (i in 0 until numTrajectories) {
                val idx = stepStartIdx + i
                if (idx >= trajectory.lat.size) break

                val iceLat = trajectory.lat[idx].toDouble()
                val iceLon = trajectory.lon[idx].toDouble()
                if (iceLat == 0.0 && iceLon == 0.0) continue

                if (haversineDistance(shipPos.lat, shipPos.lon, iceLat, iceLon) <= thresholdKm) {
                    hitsThisStep++
                }
            }
            val fraction = hitsThisStep.toDouble() / numTrajectories
            if (fraction > maxHitFraction) maxHitFraction = fraction
        }

        return maxHitFraction * 100.0
    }
}