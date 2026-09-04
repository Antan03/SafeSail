package no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers

import android.content.Context
import android.graphics.BitmapFactory
import no.uio.ifi.in2000.sofiaalo.team44.R
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconAnchor
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.android.style.layers.PropertyFactory.textAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

fun addIcebergs(style: Style, icebergFeatures: List<Feature>, context: Context) {
    val sourceId = "iceberg-source"
    val layerId = "iceberg-layer"
    val clusterLayerId = "iceberg-cluster-layer"
    val clusterCountLayerId = "iceberg-cluster-count"

    if (style.getImage("iceberg-icon") == null) {
        val original = BitmapFactory.decodeResource(context.resources, R.drawable.iceberg)
        style.addImage("iceberg-icon", original)
    }

    val geoJson = FeatureCollection.fromFeatures(icebergFeatures)

    val source = style.getSource(sourceId) as? GeoJsonSource
    if (source != null) {
        source.setGeoJson(geoJson)
    } else {
        val options = GeoJsonOptions()
            .withCluster(true)
            .withClusterRadius(25)  // Hvor tett punkter må være for å clustres (piksel)
            .withClusterMaxZoom(5) // Zoom nivå før man ser isfjell

        style.addSource(GeoJsonSource(sourceId, geoJson, options))
    }

    // Cluster-sirkler
    if (style.getLayer(clusterLayerId) == null) {
        val clusterLayer = CircleLayer(clusterLayerId, sourceId)
        clusterLayer.setFilter(Expression.has("point_count"))
        clusterLayer.setProperties(
            circleColor("#2196F3"),
            circleRadius(
                Expression.interpolate(
                    Expression.linear(),
                    Expression.get("point_count"),
                    Expression.stop(2, 15f),
                    Expression.stop(50, 25f),
                    Expression.stop(200, 35f)
                )
            ),
            circleOpacity(0.8f)
        )
        style.addLayer(clusterLayer)
    }

    // Antall i cluster
    if (style.getLayer(clusterCountLayerId) == null) {
        val countLayer = SymbolLayer(clusterCountLayerId, sourceId)
        countLayer.setFilter(Expression.has("point_count"))
        countLayer.setProperties(
            textField(Expression.toString(Expression.get("point_count"))),
            textSize(12f),
            textColor("#FFFFFF"),
            textIgnorePlacement(true),
            textAllowOverlap(true)
        )
        style.addLayer(countLayer)
    }

    // Enkeltisfjell (vises kun når ikke cluster)
    if (style.getLayer(layerId) == null) {
        val symbolLayer = SymbolLayer(layerId, sourceId)
        symbolLayer.setFilter(Expression.not(Expression.has("point_count")))
        symbolLayer.setProperties(
            iconImage("iceberg-icon"),
            iconAllowOverlap(true),
            iconAnchor(Property.ICON_ANCHOR_CENTER),
            iconSize(
                Expression.interpolate(
                    Expression.linear(),
                    Expression.zoom(),
                    Expression.stop(2, 0.3f),
                    Expression.stop(5, 0.4f),
                    Expression.stop(6, 0.5f),
                    Expression.stop(7, 0.5f),
                    Expression.stop(8, 0.5f),
                    Expression.stop(10, 0.8f),
                    Expression.stop(12, 1.0f),
                    Expression.stop(14, 1.2f),
                )
            )
        )
        style.addLayer(symbolLayer)
    }
}