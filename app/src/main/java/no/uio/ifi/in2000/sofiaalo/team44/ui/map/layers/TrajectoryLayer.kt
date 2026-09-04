package no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers

import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection

fun setupTrajectoryLayers(style: Style) {

    listOf(
        "trajectory-lines-layer",
        "trajectory-points-layer",
        "trajectory-endpoints-layer",
        "iceberg-particles",
        "iceberg-heatmap",
        "global-heatmap-layer",
        "heatmap-arrows-layer"
    ).forEach { style.removeLayer(it) }

    listOf(
        "trajectory-lines-source",
        "trajectory-points-source",
        "trajectory-endpoints-source",
        "global-heatmap-source"
    ).forEach { style.removeSource(it) }

    style.addSource(
        GeoJsonSource(
            "trajectory-lines-source",
            FeatureCollection.fromFeatures(emptyList()),
            GeoJsonOptions().withTolerance(0.5f) // forenkler geometri ved lave zoom-nivåer
        )
    )

    style.addSource(
        GeoJsonSource(
            "trajectory-points-source",
            FeatureCollection.fromFeatures(emptyList())
        )
    )

    style.addSource(
        GeoJsonSource(
            "trajectory-endpoints-source",
            FeatureCollection.fromFeatures(emptyList())
        )
    )

    // --- LAG 1: LINJER (statiske baner) ---
    // Lav opacity + farget etter hastighet → overlapp gir naturlig tetthet
    val linesLayer = LineLayer("trajectory-lines-layer", "trajectory-lines-source")
    linesLayer.setProperties(
        PropertyFactory.lineOpacity(
            Expression.interpolate(
                Expression.linear(), Expression.zoom(),
                Expression.stop(3,  0.20f),
                Expression.stop(7,  0.10f),
                Expression.stop(10, 0.06f)
            )
        ),

        PropertyFactory.lineColor(Expression.rgb(30, 144, 255)), // dodger blue

        PropertyFactory.lineWidth(
            Expression.interpolate(
                Expression.linear(), Expression.zoom(),
                Expression.stop(3,  3.0f),   // var 0.5f
                Expression.stop(6,  2.0f),
                Expression.stop(8,  1.0f),
                Expression.stop(12, 1.5f)
            )
        ),

        PropertyFactory.visibility(Property.VISIBLE)
    )
    linesLayer.minZoom = 2f
    style.addLayer(linesLayer)

    // --- LAG 2: NÅVÆRENDE POSISJONER (animert) ---
    // Disse 1000 punktene oppdateres for hvert tidssteg
    val pointsLayer = CircleLayer("trajectory-points-layer", "trajectory-points-source")
    pointsLayer.setProperties(
        PropertyFactory.circleRadius(
            Expression.interpolate(
                Expression.linear(), Expression.zoom(),
                Expression.stop(3,  6.0f),
                Expression.stop(6,  4.0f),
                Expression.stop(8,  3.0f),
                Expression.stop(12, 5.0f)
            )
        ),

        PropertyFactory.circleColor(
            Expression.interpolate(
                Expression.linear(),
                Expression.get("progress"),
                Expression.stop(0.0, Expression.rgb(100, 220, 255)),
                Expression.stop(0.5, Expression.rgb(255, 200, 0)),
                Expression.stop(1.0, Expression.rgb(220, 50,  255))
            )
        ),

        PropertyFactory.circleOpacity(0.85f),
        PropertyFactory.circleStrokeWidth(0.5f),
        PropertyFactory.circleStrokeColor("white"),
        PropertyFactory.circleStrokeOpacity(0.4f),
        PropertyFactory.visibility(Property.VISIBLE)
    )
    style.addLayer(pointsLayer)

    val endpointsLayer = CircleLayer("trajectory-endpoints-layer", "trajectory-endpoints-source")
    endpointsLayer.setProperties(
        PropertyFactory.circleRadius(
            Expression.interpolate(
                Expression.linear(), Expression.zoom(),
                Expression.stop(3,  2.0f),
                Expression.stop(8,  4.0f),
                Expression.stop(12, 7.0f)
            )
        ),
        PropertyFactory.circleColor(Expression.rgb(220, 50, 255)),
        PropertyFactory.circleOpacity(0.5f),
        PropertyFactory.circleStrokeWidth(0.5f),
        PropertyFactory.circleStrokeColor("white"),
        PropertyFactory.circleStrokeOpacity(0.3f),
        PropertyFactory.visibility(Property.VISIBLE)
    )
    endpointsLayer.minZoom = 3f
    style.addLayer(endpointsLayer)
}