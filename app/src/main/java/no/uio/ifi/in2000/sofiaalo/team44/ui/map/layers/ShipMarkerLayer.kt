package no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import no.uio.ifi.in2000.sofiaalo.team44.R
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.LatLon
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

fun updateShipMarker(style: Style, pos: LatLon, context: Context) {
    val sourceId = "ship-marker-source"
    val layerId = "ship-marker-layer"
    val imageId = "ship-icon"

    if (style.getImage(imageId) == null) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ship)
        val bitmap = (drawable as BitmapDrawable).bitmap
        style.addImage(imageId, bitmap)
    }

    val feature = Feature.fromGeometry(Point.fromLngLat(pos.lon, pos.lat))
    val featureCollection = FeatureCollection.fromFeature(feature)

    val existing = style.getSource(sourceId) as? GeoJsonSource
    if (existing != null) {
        existing.setGeoJson(featureCollection.toJson())
    } else {
        style.addSource(GeoJsonSource(sourceId).apply {
            setGeoJson(featureCollection.toJson())
        })
        style.addLayer(
            SymbolLayer(layerId, sourceId).withProperties(
                PropertyFactory.iconImage(imageId),
                PropertyFactory.iconSize(0.25f),
                PropertyFactory.iconAllowOverlap(true)
            )
        )
    }
}

fun clearShipMarker(style: Style) {
    if (style.getLayer("ship-marker-layer") != null) style.removeLayer("ship-marker-layer")
    if (style.getSource("ship-marker-source") != null) style.removeSource("ship-marker-source")
}
