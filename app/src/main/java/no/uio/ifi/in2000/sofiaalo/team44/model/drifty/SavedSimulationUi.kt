package no.uio.ifi.in2000.sofiaalo.team44.model.drifty

import java.io.File

data class SavedSimulationUi(
    val uuid: String,
    val response: DriftyIcebergSimulationResponse,
    val ncFile: File,      // uendret fil
    val jsonFile: File     // uendret fil
)