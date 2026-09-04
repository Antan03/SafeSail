package no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers


import android.content.Context
import android.graphics.BitmapFactory
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Feature
import no.uio.ifi.in2000.sofiaalo.team44.R
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.ShipRoute
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource


//Tar waypoints listen og lager en linje på kartet

fun addShipRouteLayer(style: Style, route: ShipRoute, context: Context) {
    val routeSourceId = "route-source"
    val routeLayerId  = "route-layer"

    val pointSourceId = "point-source"
    val pointLayerId = "point-layer"

    val excistingPointSource = style.getSource(pointSourceId) as? GeoJsonSource
    val existingRouteSource = style.getSource(routeSourceId) as? GeoJsonSource

    val points = route.waypoints.map { Point.fromLngLat(it.lon, it.lat) }

    if(route.waypoints.size >= 2){
        val lineFeature = Feature.fromGeometry(LineString.fromLngLats(points))
        val lineGeoJson = FeatureCollection.fromFeature(lineFeature).toJson()
        if (existingRouteSource != null) {
            existingRouteSource.setGeoJson(lineGeoJson)
        } else {
            style.addSource(GeoJsonSource(routeSourceId, lineGeoJson))
            style.addLayer(
                LineLayer(routeLayerId, routeSourceId).withProperties(
                    PropertyFactory.lineColor("#0000FF"),
                    PropertyFactory.lineWidth(2f)
                )
            )
        }
    } else {
        val emptyRouteFeatureCollection = FeatureCollection.fromFeatures(arrayOf<Feature>())
        existingRouteSource?.setGeoJson(emptyRouteFeatureCollection.toJson())
    }

    if(points.isNotEmpty()){
        val pointFeature = points.map { point ->
            Feature.fromGeometry(point)
        }
        val pointGeoJson = FeatureCollection.fromFeatures(pointFeature).toJson()

        if (style.getImage("point-icon") == null) {
            val original = BitmapFactory.decodeResource(context.resources, R.drawable.map_point)
            style.addImage("point-icon", original)
        }

        if (excistingPointSource != null) {
            excistingPointSource.setGeoJson(pointGeoJson)
        } else {
            style.addSource(GeoJsonSource(pointSourceId, pointGeoJson))
            style.addLayer(
                SymbolLayer(pointLayerId, pointSourceId).withProperties(
                    PropertyFactory.iconImage("point-icon"),
                    PropertyFactory.iconSize(0.05f),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconIgnorePlacement(true),
                    PropertyFactory.iconAnchor(""),
                    PropertyFactory.iconTranslate(arrayOf(0f, -10f))
                )
            )
        }
    }else{
        val emptyPointFeatureCollection = FeatureCollection.fromFeatures(arrayOf<Feature>())
        excistingPointSource?.setGeoJson(emptyPointFeatureCollection.toJson())
    }
}

