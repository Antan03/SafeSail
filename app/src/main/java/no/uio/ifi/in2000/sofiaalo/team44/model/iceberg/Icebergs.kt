package no.uio.ifi.in2000.sofiaalo.team44.model.iceberg

import kotlinx.serialization.Serializable

@Serializable
data class Icebergs(
    val features: List<Feature>,
    val license: String,
    val type: String
)

@Serializable
data class Feature(
    val geometry: Geometry,
    val properties: Properties,
    val type: String,
    val `when`: When
) {
    override fun toString(): String {
        var prettyPrint = ""

        prettyPrint += "Title: ${properties.title}\n"
        prettyPrint += "Breddegrad: ${geometry.coordinates[0]}\n" // dette er lengdegrad
        prettyPrint += "Lengdegrad: ${geometry.coordinates[1]}\n" // dette er breddegrad
        prettyPrint += "Areal: ${properties.BRGARE}\n"
        prettyPrint += "Maks lengde: ${properties.IA_BLN}\n"
        prettyPrint += "Lokalt konsentrasjonsnivå: ${properties.IA_BCN}\n"
        prettyPrint += "Timestamp: ${`when`.instant}\n"

        return prettyPrint}
}

@Serializable
data class Geometry(
    val coordinates: List<Double>,
    val type: String
)

@Serializable
data class Properties(
    val BRGARE: Double,
    val IA_BCN: Double,
    val IA_BLN: Double,
    val ais_chk: String,
    val title: String
)

@Serializable
data class When(
    val instant: String
)