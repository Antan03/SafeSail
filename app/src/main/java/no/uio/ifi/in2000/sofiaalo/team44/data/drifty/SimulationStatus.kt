package no.uio.ifi.in2000.sofiaalo.team44.data.drifty

import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.DriftyIcebergSimulationResponse

sealed class SimulationStatus {

    object Starting : SimulationStatus()
    object Idle : SimulationStatus()
    data class Waiting(val seconds: Int) : SimulationStatus()
    data class Success(
        val data: DriftyIcebergSimulationResponse,
        val totalSecondsElapsed: Int
    ) : SimulationStatus()
    data class Error(val message: String) : SimulationStatus()
}