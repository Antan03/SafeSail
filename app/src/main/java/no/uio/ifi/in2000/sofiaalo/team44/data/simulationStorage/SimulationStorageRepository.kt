package no.uio.ifi.in2000.sofiaalo.team44.data.simulationStorage

import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.DriftyIcebergSimulationResponse
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.IcebergTrajectoryData
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.ResponseSpec
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.SavedSimulationUi
import java.io.File

/*
- Brukes i hovedsak av DriftyViewModel
- Gjør alt med lagring av simuleringer:
    - Laste ned responsen
    - lese simuleringsFila
    - hente lagra simuleringer fra disk
    - lage id-er

 */
interface SimulationStorageRepository {
    suspend fun downloadNetCDF(url: String, response: DriftyIcebergSimulationResponse): File?
    suspend fun readTrajectory(file: File): IcebergTrajectoryData
    suspend fun loadSavedSimulations(): List<SavedSimulationUi>
    fun generateStableId(spec: ResponseSpec): String
}