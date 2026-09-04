package no.uio.ifi.in2000.sofiaalo.team44.model.drifty

import kotlinx.serialization.Serializable


@Serializable
data class DriftyIcebergSimulationResponse(
    val result: ResponseResult,
    val spec: ResponseSpec
)

@Serializable
data class ResponseResult(
    val files: ResponseFiles,
    val status: ResponseStatus
)

@Serializable
data class ResponseSpec(
    val configuration: ResponseConfiguration,
    val geo: ResponseGeo,
    val model: String,
    val simulationDurationInHours: Int
)

@Serializable
data class ResponseFiles(
    val log: String,
    val main: String? = null,
    val plot: String? = null
)

@Serializable
data class ResponseStatus(
    val success: Boolean
)

@Serializable
data class ResponseConfiguration(
    val seed: ResponseSeed
)

@Serializable
data class ResponseGeo(
    val point: ResponsePoint
)

@Serializable
data class ResponseSeed(
    val draft: Double,
    val length: Double,
    val sail: Double,
    val width: Double
)

@Serializable
data class ResponsePoint(
    val latitude: Double,
    val longitude: Double,
    val radiusInMeters: Int,
    val time: String
)