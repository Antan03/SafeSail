package no.uio.ifi.in2000.sofiaalo.team44.model.map

data class MapLayers(
    // her putter vi alle lag som går over kartet, noen av disse er eksempler
    val showVictoriaLayer: Boolean = false,
    val showSimulation: Boolean = true,
    val showIcebergs: Boolean = true,
    val showShipRoute: Boolean = true,
    val showMetAlerts: Boolean = true
)