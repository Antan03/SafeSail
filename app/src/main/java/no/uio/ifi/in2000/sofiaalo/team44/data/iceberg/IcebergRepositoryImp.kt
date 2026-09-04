package no.uio.ifi.in2000.sofiaalo.team44.data.iceberg

import no.uio.ifi.in2000.sofiaalo.team44.model.iceberg.Feature
import no.uio.ifi.in2000.sofiaalo.team44.model.iceberg.Icebergs

class IcebergRepositoryImp(
    private val icebergDataSource: IcebergDataSource = IcebergDataSource()
) : IcebergRepository {

    override suspend fun getIcebergsFromSource(timestamp: Long): Icebergs {
        val icebergs: Icebergs = icebergDataSource.getIcebergsInfoFromSource(timestamp)
        return icebergs.copy(features = filterDuplicates(icebergs.features))
    }

    private fun filterDuplicates(features: List<Feature>): List<Feature> {
        return features
            .sortedByDescending { it.`when`.instant }
            .distinctBy { feature ->
                val lat = "%.1f".format(feature.geometry.coordinates[1])
                val lon = "%.1f".format(feature.geometry.coordinates[0])
                "$lat$lon"
            }
    }
}
