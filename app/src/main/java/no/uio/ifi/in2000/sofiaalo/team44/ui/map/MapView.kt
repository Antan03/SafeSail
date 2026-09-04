package no.uio.ifi.in2000.sofiaalo.team44.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.uio.ifi.in2000.sofiaalo.team44.model.map.MapLayers
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.ShipRoute
import no.uio.ifi.in2000.sofiaalo.team44.ui.components.TrajectoryRenderer
import no.uio.ifi.in2000.sofiaalo.team44.ui.components.highlightRiskIceberg
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.addCircle
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.addIcebergs
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.addMetAlertsPolygons
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.addShipRouteLayer
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.addVictoriaLayer
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.clearShipMarker
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.setupTrajectoryLayers
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.layers.updateShipMarker
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.PlaybackViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.SliderMode
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.geojson.Point
import no.uio.ifi.in2000.sofiaalo.team44.model.iceberg.Feature as IcebergFeature
import no.uio.ifi.in2000.sofiaalo.team44.model.metAlerts.Feature as MetAlertsFeature


@Composable
fun MapView(
    wmsUrl: String?,
    icebergFeatures: List<IcebergFeature>,
    metAlertsFeatures: List<MetAlertsFeature>,
    route: ShipRoute,
    currentIndex: Int,
    layers: MapLayers,
    onMapClick: (Double, Double) -> Unit,
    onIcebergClick: (Double, Double) -> Unit,
    playbackViewModel: PlaybackViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var styleRef by remember { mutableStateOf<Style?>(null) }

    val sliderMode by playbackViewModel.sliderMode.collectAsStateWithLifecycle()
    val mapView = remember {
        MapView(context).apply { onCreate(null) }
    }

    // Livssyklushåndtering for MapLibre
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START   -> mapView.onStart()
                Lifecycle.Event.ON_RESUME  -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE   -> mapView.onPause()
                Lifecycle.Event.ON_STOP    -> mapView.onStop()
               else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            styleRef = null
            mapView.onDestroy()
        }
    }

    // Initial oppsett av kartet
    LaunchedEffect(Unit) {
        mapView.getMapAsync { map ->
            map.uiSettings.isRotateGesturesEnabled = false
            map.setStyle("https://demotiles.maplibre.org/style.json") { style ->
                styleRef = style
                setupTrajectoryLayers(style)
                layers(style, wmsUrl, icebergFeatures, context, metAlertsFeatures, route)
                addCircle(emptyList(), style)
            }
            map.addOnCameraMoveStartedListener { // Viktig: stopp animasjoner når man beveger kameraet, ellers konkurreres det om samme tråd og det blir kræsj
                playbackViewModel.pauseForCamera()
            }
            map.addOnCameraIdleListener {
                playbackViewModel.resumeAfterCamera()
            }

            map.addOnMapClickListener { point ->
                val screenPoint = map.projection.toScreenLocation(point)
                val rect = android.graphics.RectF(screenPoint.x - 20f,
                    screenPoint.y,
                    screenPoint.x,
                    screenPoint.y)
                val iceFeatures = map.queryRenderedFeatures(rect, "iceberg-layer")
                val geo = iceFeatures.firstOrNull()?.geometry() as? Point
                if (geo != null) {
                    onIcebergClick(geo.latitude(), geo.longitude())
                    return@addOnMapClickListener true
                }

                val clusterFeatures = map.queryRenderedFeatures(rect, "iceberg-cluster-layer")
                if (clusterFeatures.isNotEmpty()) {
                    val clusterPoint = clusterFeatures.first().geometry() as? Point
                    if (clusterPoint != null) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(clusterPoint.latitude(), clusterPoint.longitude()),
                            map.cameraPosition.zoom + 1.25
                        ))
                    }
                    return@addOnMapClickListener true
                }

                val landFeatures = map.queryRenderedFeatures(screenPoint, "countries-fill")

                if (landFeatures.isEmpty()) {
                    // traff ikke land, isfell eller cluster så må være vann. surely
                    onMapClick(point.latitude, point.longitude)
                }
                true
            }
        }
    }

    // Oppdatering av statiske lag (Victoria, Isfjell-baner, Met-varsler)
    LaunchedEffect(wmsUrl, icebergFeatures, metAlertsFeatures, route, layers, sliderMode, styleRef) {
        val style = styleRef ?: return@LaunchedEffect

        if (wmsUrl != null) {
            addVictoriaLayer(style, wmsUrl)
            style.getLayer("victoria-layer")?.setProperties(
                PropertyFactory.visibility(if (layers.showVictoriaLayer) Property.VISIBLE else Property.NONE)
            )
        } else {
            style.removeLayer("victoria-layer")
            style.removeSource("victoria-source")
        }

        addIcebergs(style, toGeoFeatures(icebergFeatures), context)
        addMetAlertsPolygons(style, metAlertsFeatures)
        addShipRouteLayer(style, route, context)

        val visibility = { show: Boolean -> if (show) Property.VISIBLE else Property.NONE }
        style.getLayer("iceberg-layer")?.setProperties(PropertyFactory.visibility(visibility(layers.showIcebergs)))
        style.getLayer("iceberg-cluster-layer")?.setProperties(PropertyFactory.visibility(visibility(layers.showIcebergs)))
        style.getLayer("iceberg-cluster-count")?.setProperties(PropertyFactory.visibility(visibility(layers.showIcebergs)))
        style.getLayer("risk-circles-layer")?.setProperties(PropertyFactory.visibility(visibility(layers.showIcebergs)))

        style.getLayer("polygon-layer")?.setProperties(PropertyFactory.visibility(visibility(layers.showMetAlerts)))

        val routeVisible = layers.showShipRoute && sliderMode == SliderMode.SHIP_ROUTE
        style.getLayer("route-layer")?.setProperties(PropertyFactory.visibility(visibility(routeVisible)))

        if (sliderMode == SliderMode.SIMULATION) clearShipMarker(style)
        style.getLayer("global-heatmap-layer")?.setProperties(PropertyFactory.visibility(visibility(layers.showSimulation)))

        if (!layers.showShipRoute) clearShipMarker(style)
    }

    // Skip-animasjon
    LaunchedEffect(currentIndex, route,sliderMode) {
        val style = styleRef ?: return@LaunchedEffect

        if (sliderMode == SliderMode.SIMULATION) {
            clearShipMarker(style)
            return@LaunchedEffect
        }

        if (route.waypoints.size < 2 || !layers.showShipRoute) return@LaunchedEffect

        val pos = route.getPositionAtStep(currentIndex)
        updateShipMarker(style, pos, context)
        val highlights = highlightRiskIceberg(pos, icebergFeatures)
        addCircle(highlights, style)
    }

    styleRef?.let { style ->
        TrajectoryRenderer(
            mapView = mapView,
            style = style,
            viewModel = playbackViewModel,
            isVisible = layers.showSimulation
        )
    }
    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

}