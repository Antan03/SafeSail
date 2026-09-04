package no.uio.ifi.in2000.sofiaalo.team44.model.ship

import no.uio.ifi.in2000.sofiaalo.team44.util.haversineDistance

data class Waypoint(
    val lat: Double,
    val lon: Double,
    val label: String? = null
)

data class RouteLeg(
    val from: Waypoint,
    val to: Waypoint,
    val speedKnots: Double = 12.0
) {
    val distanceNm: Double get() =
        haversineDistance(from.lat, from.lon, to.lat, to.lon)

    val durationHours: Double get() =
        if (speedKnots > 0) distanceNm / speedKnots else 0.0

    val durationMs: Long get() =
        (durationHours * 3_600_000).toLong()
}

data class ShipRoute(
    val waypoints: List<Waypoint> = emptyList(),
    val defaultSpeedKnots: Double = 12.0,
    val startTime: Long = System.currentTimeMillis()
) {
    val legs: List<RouteLeg> get() {
        if (waypoints.size < 2) return emptyList()
        return waypoints.zipWithNext { a, b -> RouteLeg(a, b, defaultSpeedKnots) }
    }

    val totalDistanceNm: Double get() = legs.sumOf { it.distanceNm }
    val totalDurationHours: Double get() = legs.sumOf { it.durationHours }

    val totalHours: Int get() = totalDurationHours.toInt().coerceAtLeast(1)
    fun withWaypoint(waypoint: Waypoint) = copy(
        waypoints = waypoints + waypoint
    )

    fun removeWayPoint(point: Waypoint): ShipRoute {
        val newWaypoints = waypoints.filter { it != point }

        return copy(waypoints = newWaypoints)
    }

    fun calculateTimestamps(): List<Pair<Waypoint, Long>> {
        if (waypoints.isEmpty()) return emptyList()

        val result = mutableListOf<Pair<Waypoint, Long>>()
        var currentTime = startTime

        result.add(waypoints[0] to currentTime)

        for (i in 0 until legs.size) {
            currentTime += legs[i].durationMs
            result.add(waypoints[i + 1] to currentTime)
        }
        return result
    }
    fun getPositionAtStep(index: Int): LatLon {

        val hourInMs = 3_600_000L
        val timeAtStep = this.startTime + (index * hourInMs)

        val timedWaypoints = calculateTimestamps()
        return getPositionAtTime(timeAtStep, timedWaypoints)
    }
    fun getInterpolatedRoute(intervalMs: Long = 3_600_000): List<LatLon> {
        if (waypoints.isEmpty()) return emptyList()

        val result = mutableListOf<LatLon>()
        val timedWaypoints = calculateTimestamps()



        val totalDuration = legs.sumOf { it.durationMs }
        val startTime = startTime
        val endTime = startTime + totalDuration

        for (time in startTime..endTime step intervalMs) {
            val pos = getPositionAtTime(time, timedWaypoints)
            result.add(pos)
        }

        return result
    }

    private fun getPositionAtTime(time: Long, timedWaypoints: List<Pair<Waypoint, Long>>): LatLon {

        if (timedWaypoints.isEmpty()) {
            return LatLon(0.0, 0.0)
        }
        if (timedWaypoints.size == 1) {
            return LatLon(timedWaypoints[0].first.lat, timedWaypoints[0].first.lon)
        }

        if (time <= timedWaypoints.first().second) {
            return LatLon(timedWaypoints.first().first.lat, timedWaypoints.first().first.lon)
        }
        if (time >= timedWaypoints.last().second) {
            return LatLon(timedWaypoints.last().first.lat, timedWaypoints.last().first.lon)
        }

        for (i in 0 until timedWaypoints.size - 1) {
            val (startWp, sTime) = timedWaypoints[i]
            val (endWp, eTime) = timedWaypoints[i + 1]

            if (time in sTime..eTime) {
                val duration = eTime - sTime
                if (duration == 0L) return LatLon(startWp.lat, startWp.lon)

                val fraction = (time - sTime).toDouble() / duration.toDouble()
                val lat = startWp.lat + (endWp.lat - startWp.lat) * fraction
                val lon = startWp.lon + (endWp.lon - startWp.lon) * fraction
                return LatLon(lat, lon)
            }
        }
        return LatLon(timedWaypoints.last().first.lat, timedWaypoints.last().first.lon)
    }

    fun waypointIndices(): List<Int> {

        if (waypoints.isEmpty()) return emptyList()
        var currentHours = 0.0
        val indices = mutableListOf(0)
        legs.forEach {
            currentHours += it.durationHours
            indices.add(currentHours.toInt())
        }
        return indices
    }
    fun buildTimeSteps(startTime: Long = this.startTime): List<Long> = List(totalHours + 1) { i ->
        startTime + (i * 3_600_000L)
    }

}