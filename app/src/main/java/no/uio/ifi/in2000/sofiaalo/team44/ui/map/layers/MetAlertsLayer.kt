package no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers

import kotlinx.serialization.json.jsonArray
import no.uio.ifi.in2000.sofiaalo.team44.model.metAlerts.Feature
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource

fun addMetAlertsPolygons(style: Style, metAlertsFeatures: List<Feature>) {

    val featuresJson = metAlertsFeatures.joinToString(",") { f ->

        val listOfCoordinates = f.geometry.coordinates.jsonArray
        var riskColor = f.properties.riskMatrixColor
        if ( riskColor == null) {
            val severity = f.properties.severity
            when (severity){
                "Minor" -> riskColor = "Green"
                "Moderate" -> riskColor = "Yellow"
                "Severe" -> riskColor = "Orange"
                "Extreme" -> riskColor = "Red"
            }
        }

        if (f.geometry.type == "MultiPolygon") {
            //NEW SHIT
            val newString = listOfCoordinates.joinToString(",") { item ->
            """
                {
                    "type":"Feature",
                    "geometry":
                    {
                        "type":"Polygon",
                        "coordinates": $item
                    },
                    "properties": 
                    {
                        "riskMatrixColor": "$riskColor"
                    }
                }
            """.trimIndent()
            }
            newString
        }

        else {

            """
            {
                "type":"Feature",
                "geometry":
                {
                    "type":"Polygon",
                    "coordinates": $listOfCoordinates
                },
                "properties": 
                {
                    "riskMatrixColor": "$riskColor"
                }
            }
        """.trimIndent()
        }


    }

    //Log.d("debug", featuresJson)

    val geoJson =
        """
            {
                "type":"FeatureCollection",
                "features":[$featuresJson]}
        """.trimIndent()

    style.getSource("polygon-source")?.let {
        (it as GeoJsonSource).setGeoJson(geoJson)
    } ?: run {
        style.addSource(GeoJsonSource("polygon-source", geoJson))
        val layer = FillLayer("polygon-layer", "polygon-source").withProperties(
            PropertyFactory.fillColor(
                org.maplibre.android.style.expressions.Expression.get("riskMatrixColor")
            ),
            PropertyFactory.fillOpacity(0.6f),
            PropertyFactory.fillOutlineColor("#000000")
        )
        style.addLayer(layer)
    }
}
