package no.uio.ifi.in2000.sofiaalo.team44.data.metAlerts

import no.uio.ifi.in2000.sofiaalo.team44.model.metAlerts.MetAlerts

interface MetAlertsRepository {
    suspend fun getMetAlertsFromSource(timestamp: Long): MetAlerts
}
