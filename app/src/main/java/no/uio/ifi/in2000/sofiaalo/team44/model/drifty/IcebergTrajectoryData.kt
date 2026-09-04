package no.uio.ifi.in2000.sofiaalo.team44.model.drifty


data class IcebergTrajectoryData(
    val lat: List<Float>,
    val lon: List<Float>,
    val icebergXVelocity: List<Float>,
    val icebergYVelocity: List<Float>,
    val time: List<Float>
)
// Dumma ned siden jeg bruker de færreste variablene og det tar opp veldig mye plass
/*

data class IcebergTrajectoryData(
    // Posisjon og bevegelse
    val lat: List<Float>,
    val lon: List<Float>,
    val z: List<Float>,              // dybde
    val ageSeconds: List<Float>,
    val status: List<Float>,
    val moving: List<Float>,

    // Isfjell-egenskaper
    val sail: List<Float>,           // høyde over vann
    val draft: List<Float>,          // dybde under vann
    val length: List<Float>,
    val width: List<Float>,

    // Hastigheter
    val icebergXVelocity: List<Float>,
    val icebergYVelocity: List<Float>,
    val xSeaWaterVelocity: List<Float>,
    val ySeaWaterVelocity: List<Float>,

    // Vær og hav
    val xWind: List<Float>,
    val yWind: List<Float>,
    val waveHeight: List<Float>,
    val seaWaterTemperature: List<Float>,
    val seaIceFraction: List<Float>,

    // Tid
    val time: List<Float>
)

 */
