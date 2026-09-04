package no.uio.ifi.in2000.sofiaalo.team44.data.metAlerts

import no.uio.ifi.in2000.sofiaalo.team44.model.metAlerts.MetAlerts

class MetAlertsRepositoryImp(
    private val dataSource: MetAlertsDataSource = MetAlertsDataSource()
) : MetAlertsRepository {

    override suspend fun getMetAlertsFromSource(timestamp: Long): MetAlerts {
        return dataSource.getMetAlertsFromSource(timestamp)
    }
}
