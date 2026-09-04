package no.uio.ifi.in2000.sofiaalo.team44.ui.map

import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.addShipRouteLayer
import android.content.Context
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.ShipRoute
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.addIcebergs
import no.uio.ifi.in2000.sofiaalo.team44.model.iceberg.Feature as IcebergFeature
import no.uio.ifi.in2000.sofiaalo.team44.model.metAlerts.Feature as MetAlertsFeature
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.addMetAlertsPolygons
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.addVictoriaLayer
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Feature as GeoFeature
import org.maplibre.geojson.Point

fun toGeoFeatures(features: List<IcebergFeature>): List<GeoFeature> {
    return features.map { f ->
        val lon = f.geometry.coordinates[0]
        val lat = f.geometry.coordinates[1]
        val size = f.properties.IA_BLN
        GeoFeature.fromGeometry(
            Point.fromLngLat(lon, lat)
        ).apply {
            addNumberProperty("size", size)
        }
    }
}

fun layers(
    style: Style,
    wmsUrl: String?,
    icebergFeatures: List<IcebergFeature>,
    context: Context,
    metAlertsFeatures: List<MetAlertsFeature>,
    route: ShipRoute
) {
    if (wmsUrl != null) addVictoriaLayer(style, wmsUrl)
    addMetAlertsPolygons(style, metAlertsFeatures)
    addIcebergs(style, toGeoFeatures(icebergFeatures), context)
    addShipRouteLayer(style, route, context)
}