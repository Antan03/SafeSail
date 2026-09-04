package no.uio.ifi.in2000.sofiaalo.team44.model.metAlerts

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MetAlerts(
    val features: List<Feature>,
    val lang: String,
    val lastChange: String,
    val type: String
)

@Serializable
data class Feature(
    val geometry: Geometry,
    val properties: Properties,
    val type: String,
    val `when`: When
)

@Serializable
data class Geometry(
    val coordinates: JsonElement,   // JsonElement fordi strukturen kan variere
    val type: String
)

@Serializable
data class Properties(
    val altitudeAboveSeaLevel: Int? = null,
    val area: String? = null,
    val awarenessResponse: String? = null,
    val awarenessSeriousness: String? = null,
    val awarenessLevel: String? = null,
    val awarenessType: String? = null,
    val ceilingAboveSeaLevel: Int? = null,
    val certainty: String? = null,
    val consequences: String? = null,
    val contact: String? = null,
    val county: List<String>? = null,
    val description: String? = null,
    val event: String? = null,
    val eventAwarenessName: String? = null,
    val eventEndingTime: String? = null,
    val geographicDomain: String? = null,
    val id: String? = null,
    val instruction: String? = null,
    val resources: List<Resource>? = null,
    val riskMatrixColor: String? = null,
    val severity: String? = null,
    val status: String? = null,
    val title: String? = null,
    val triggerLevel: String? = null,
    val type: String? = null,
    val web: String? = null
)

@Serializable
data class Resource(
    val description: String? = null,
    val mimeType: String? = null,
    val uri: String? = null
)

@Serializable
data class When(
    val interval: List<String>
)