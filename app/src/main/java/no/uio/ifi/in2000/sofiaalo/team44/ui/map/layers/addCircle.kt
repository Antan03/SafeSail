package no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers

import android.graphics.Color
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.LatLon
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.android.style.expressions.Expression as Exp


fun addCircle(latlongs: List<Pair<LatLon, Double>>, style: Style){

    // gjør om alle punktene eller risikoene til features
    val features = latlongs.map { (pos, dist) ->
        val en = Feature.fromGeometry(Point.fromLngLat(pos.lon, pos.lat))
        en.addNumberProperty("distance", dist)
        en
    }
    val feature = FeatureCollection.fromFeatures(features)

    val existingSource = style.getSourceAs("risk-circles-source") as? GeoJsonSource
    if (existingSource != null) {
        existingSource.setGeoJson(feature)
    } else {
        style.addSource(GeoJsonSource("risk-circles-source", feature))
        // skal være ganske enkel å forstå, men bruker distance til å automagisk velge en farge
        val color = circleColor(
            Exp.step(
                Exp.get("distance"),
                Exp.color(Color.RED), // hvis isfjell/feature er innenfor 25km
                Exp.stop(25.0, Exp.color(Color.YELLOW)), // 25 - 75
                Exp.stop(75.0, Exp.color(Color.GREEN)) // 75 - 150
            )
        )
        style.addLayer(
            CircleLayer("risk-circles-layer", "risk-circles-source").withProperties(
                circleRadius(10f),
                color,
                circleOpacity(0.5f)
            )
        )
    }
}