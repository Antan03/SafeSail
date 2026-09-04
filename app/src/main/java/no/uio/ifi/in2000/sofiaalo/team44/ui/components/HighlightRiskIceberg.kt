package no.uio.ifi.in2000.sofiaalo.team44.ui.components

import no.uio.ifi.in2000.sofiaalo.team44.model.iceberg.Feature
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.LatLon
import no.uio.ifi.in2000.sofiaalo.team44.util.haversineDistance




fun highlightRiskIceberg(skipos: LatLon, icebergs: List<Feature>) : List<Pair<LatLon, Double>>{
    val ret = mutableListOf<Pair<LatLon, Double>>()
    icebergs.forEach {
        val lat2 = it.geometry.coordinates[1]
        val lon2 = it.geometry.coordinates[0]
        val dist = haversineDistance(lat1 = skipos.lat, lon1 = skipos.lon,
            lat2 = lat2,lon2 )

        // brukte 100 fordi 4000 er det maksimale et isfjell har flytta seg på en uke, så valgte bare noe basic
        // Fordi isfjellene skal i teorien være maks fra en uke siden
        // mulig at dist må økes, for å vise flere isfjell.
        // haversine returnerer i meter

        if( dist <= 150){
            ret.add(Pair(LatLon(lat2, lon2), dist))
        }

    }
    return ret
}