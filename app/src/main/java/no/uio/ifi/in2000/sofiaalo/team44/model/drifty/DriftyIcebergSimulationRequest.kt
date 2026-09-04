package no.uio.ifi.in2000.sofiaalo.team44.model.drifty
import kotlinx.serialization.Serializable
@Serializable
data class DriftyIceBergSimulationRequest(
    val configuration: Configuration,
    val geo: Geo,
    val model: String,
    val simulationDurationInHours: Int
)
@Serializable
data class Configuration(
    val seed: Seed
)
@Serializable
data class Geo(
    val point: Point
)
@Serializable
data class Seed(
    val draft: Double,
    val length: Double,
    val sail: Double,
    val width: Double
)
@Serializable
data class Point(
    val latitude: Double,
    val longitude: Double,
    val radiusInMeters: Int,
    val time: String
)
