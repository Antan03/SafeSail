package no.uio.ifi.in2000.sofiaalo.team44.data.drifty

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.DriftyIceBergSimulationRequest
/*
- Tar api-kall fra DriftyDataSource
- Styrer logikken med post & get
- Putter hver del i en flow med statusene:
    - Starting
    - Waiting
    - Success
    - Error
 */

interface DriftyRepository {
    fun getSimulationResult(
        request: DriftyIceBergSimulationRequest
    ): Flow<SimulationStatus>
}