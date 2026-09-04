package no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet

fun addVictoriaLayer(style: Style, wmsUrl: String) {
    style.getLayer("victoria-layer")?.let { style.removeLayer("victoria-layer") }
    style.getSource("victoria-source")?.let { style.removeSource("victoria-source") }

    style.addSource(RasterSource("victoria-source", TileSet("tileset", wmsUrl), 256))

    val iceLayer = RasterLayer("victoria-layer", "victoria-source")

    if (style.getLayer("trajectory-lines-layer") != null) {
        style.addLayerBelow(iceLayer, "trajectory-lines-layer")
    } else {
        style.addLayer(iceLayer)
    }
}