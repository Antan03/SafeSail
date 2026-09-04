package no.uio.ifi.in2000.sofiaalo.team44.util

import kotlin.math.*

fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    //Convert to radians
    val lat1_rad = Math.toRadians(lat1)
    val lon1_rad = Math.toRadians(lon1)
    val lat2_rad = Math.toRadians(lat2)
    val lon2_rad = Math.toRadians(lon2)

    //Difference in coordinates
    val delta_lat = lat2_rad - lat1_rad
    val delta_lon = lon2_rad - lon1_rad

    //Haversine formula
    val a = sin(delta_lat / 2).pow(2) + cos(lat1_rad) * cos(lat2_rad) * sin(delta_lon/2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    //Earth radius in kilometers
    val R = 6371.0
    val distance = R * c

    return distance
}

fun computeDistance(list: List<Pair<Double, Double>>): Double {
    var distance: Double = 0.0
    if (list.size < 2) return distance

    //Adds up the distances between all coordinate pairs
    for (i in 0..<(list.size - 1)) {
        distance += haversineDistance(list[i].first, list[i].second, list[i+1].first, list[i+1].second)
    }
    return distance
}