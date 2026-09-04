package no.uio.ifi.in2000.sofiaalo.team44.ui.components


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.PlaybackViewModel
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.MultiPoint
import org.maplibre.geojson.Point

@Composable
fun TrajectoryRenderer(
    mapView: MapView,
    style: Style,
    viewModel: PlaybackViewModel,
    isVisible: Boolean
) {

    val activeIcebergs by viewModel.visibleIcebergs.collectAsStateWithLifecycle()
    val trajectories   by viewModel.trajectories.collectAsStateWithLifecycle()
    val globalTimeIndex by viewModel.globalTimeIndex.collectAsStateWithLifecycle()
    val particleCount by viewModel.particleCount.collectAsStateWithLifecycle()
    val showTrajectoryLines by viewModel.showTrajectoryLines.collectAsStateWithLifecycle()
    val lineOpacity by viewModel.lineOpacity.collectAsStateWithLifecycle()

    val pointsGeneration = remember { intArrayOf(0) }

    LaunchedEffect(lineOpacity) {
        if (!style.isFullyLoaded) return@LaunchedEffect
        mapView.post {
            style.getLayer("trajectory-lines-layer")
                ?.setProperties(PropertyFactory.lineOpacity(lineOpacity))
        }
    }

    LaunchedEffect(activeIcebergs, trajectories, isVisible, showTrajectoryLines) {
        if (!style.isFullyLoaded) return@LaunchedEffect

        val lineFeatures = mutableListOf<Feature>()

        activeIcebergs.forEach { iceberg ->
            val traj = trajectories[iceberg.uuid] ?: return@forEach
            val numTimeSteps = traj.time.size
            if (numTimeSteps < 2) return@forEach

            val numTrajectories = minOf(traj.lat.size / numTimeSteps, particleCount).coerceAtLeast(1)

            repeat(numTrajectories) { trajectoryIndex ->
                val startIdx = trajectoryIndex * numTimeSteps
                val endIdx   = startIdx + numTimeSteps

                val coordinates = (startIdx until endIdx).map { i ->
                    Point.fromLngLat(
                        traj.lon[i].toDouble(),
                        traj.lat[i].toDouble()
                    )
                }

                val geometry = org.maplibre.geojson.LineString.fromLngLats(coordinates)
                val feature  = Feature.fromGeometry(geometry)
                feature.addStringProperty("icebergId", iceberg.uuid)
                lineFeatures.add(feature)
            }
        }

        val collection = FeatureCollection.fromFeatures(lineFeatures)

        mapView.post {
            if (!style.isFullyLoaded) return@post
            val source = style.getSourceAs<GeoJsonSource>("trajectory-lines-source") ?: return@post
            source.setGeoJson(collection)

            val visibility = if (isVisible) Property.VISIBLE else Property.NONE
            val lineVisibility = if (isVisible && showTrajectoryLines) Property.VISIBLE else Property.NONE
            style.getLayer("trajectory-lines-layer")?.setProperties(PropertyFactory.visibility(lineVisibility))
            style.getLayer("trajectory-points-layer")?.setProperties(PropertyFactory.visibility(visibility))
            style.getLayer("trajectory-endpoints-layer")?.setProperties(PropertyFactory.visibility(lineVisibility))
        }
    }

    LaunchedEffect(activeIcebergs, trajectories, isVisible, particleCount) {
        if (!style.isFullyLoaded) return@LaunchedEffect

        val endpointPoints = mutableListOf<Feature>()

        activeIcebergs.forEach { iceberg ->
            val traj = trajectories[iceberg.uuid] ?: return@forEach
            val numTimeSteps = traj.time.size
            if (numTimeSteps < 1) return@forEach
            val numTrajectories = minOf(traj.lat.size / numTimeSteps, particleCount).coerceAtLeast(1)
            val lastTimeIndex = numTimeSteps - 1

            repeat(numTrajectories) { trajectoryIndex ->
                val lastIdx = trajectoryIndex * numTimeSteps + lastTimeIndex
                if (lastIdx < traj.lat.size) {
                    endpointPoints.add(Feature.fromGeometry(
                        Point.fromLngLat(traj.lon[lastIdx].toDouble(), traj.lat[lastIdx].toDouble())
                    ))
                }
            }
        }

        val endpointCollection = FeatureCollection.fromFeatures(endpointPoints)
        mapView.post {
            if (!style.isFullyLoaded) return@post
            style.getSourceAs<GeoJsonSource>("trajectory-endpoints-source")
                ?.setGeoJson(endpointCollection)
        }
    }

    LaunchedEffect(globalTimeIndex, activeIcebergs, trajectories, isVisible, particleCount) {
        if (!style.isFullyLoaded) return@LaunchedEffect
        if (!isVisible) return@LaunchedEffect

        delay(32)

        val features = mutableListOf<Feature>()

        activeIcebergs.forEach { iceberg ->
            val traj = trajectories[iceberg.uuid] ?: return@forEach
            val numTimeSteps = traj.time.size
            if (numTimeSteps < 1) return@forEach
            val numTrajectories = minOf(traj.lat.size / numTimeSteps, particleCount).coerceAtLeast(1)

            val safeTimeIndex = globalTimeIndex.coerceIn(0, numTimeSteps - 1)
            val lastTimeIndex = numTimeSteps - 1

            val coords = ArrayList<Point>(numTrajectories)
            repeat(numTrajectories) { trajectoryIndex ->
                val currentIdx = trajectoryIndex * numTimeSteps + safeTimeIndex
                if (currentIdx < traj.lat.size) {
                    coords.add(Point.fromLngLat(
                        traj.lon[currentIdx].toDouble(),
                        traj.lat[currentIdx].toDouble()
                    ))
                }
            }

            if (coords.isNotEmpty()) {
                val feature = Feature.fromGeometry(MultiPoint.fromLngLats(coords))
                feature.addStringProperty("icebergId", iceberg.uuid)
                feature.addNumberProperty("timeIndex", safeTimeIndex)
                feature.addNumberProperty("progress",
                    safeTimeIndex.toDouble() / lastTimeIndex.toDouble()
                )
                features.add(feature)
            }
        }

        val generation = ++pointsGeneration[0]
        val currentCollection = FeatureCollection.fromFeatures(features)
        mapView.post {
            if (!style.isFullyLoaded) return@post
            if (pointsGeneration[0] != generation) return@post
            style.getSourceAs<GeoJsonSource>("trajectory-points-source")
                ?.setGeoJson(currentCollection)
        }
    }
}