package no.uio.ifi.in2000.sofiaalo.team44.data.iceberg

import no.uio.ifi.in2000.sofiaalo.team44.model.iceberg.Icebergs

interface IcebergRepository {
    suspend fun getIcebergsFromSource(timestamp: Long): Icebergs
}
