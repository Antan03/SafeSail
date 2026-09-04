package no.uio.ifi.in2000.sofiaalo.team44.data.simulationStorage

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.sofiaalo.team44.data.fileStorage.FileStorage
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.DriftyIcebergSimulationResponse
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.IcebergTrajectoryData
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.ResponseSpec
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.SavedSimulationUi
import ucar.nc2.dataset.NetcdfDatasets
import java.io.File

class SimulationStorageRepositoryImp(
    private val fileStorage: FileStorage,
    private val client: HttpClient

) : SimulationStorageRepository {

    private val dir get() = fileStorage.filesDir
    override fun generateStableId(spec: ResponseSpec): String {
        val config = spec.configuration.seed
        val point = spec.geo.point

        val rawString =
            "${"%.4f".format(point.latitude)}${"%.4f".format(point.longitude)}${point.time}${point.radiusInMeters}" +
                    "${config.width}${config.length}${config.draft}${config.sail}" +
                    "${spec.simulationDurationInHours}${spec.model}"

        return rawString.hashCode().toString()
    }

    override suspend fun downloadNetCDF(
        url: String,
        response: DriftyIcebergSimulationResponse
    ): File? {

        val stableId = generateStableId(response.spec)

        val ncFile = File(dir, "iceberg_$stableId.nc")
        val jsonFile = File(dir, "iceberg_$stableId.json")

        try {
            if (!jsonFile.exists()) {
                jsonFile.writeText(Json.encodeToString(response))
            }
        } catch (e: Exception) {
            Log.e("STORAGE", "Feil ved lagring av JSON: ${e.message}")
            return null
        }

        if (ncFile.exists() && ncFile.length() > 0) {
            return ncFile
        }

        return try {
            val httpResponse = client.get { url(url) }

            if (httpResponse.status == HttpStatusCode.OK) {
                val bytes = httpResponse.readRawBytes()
                ncFile.writeBytes(bytes)
                ncFile
            } else {
                Log.w("STORAGE", "Uventet HTTP-status ved nedlasting: ${httpResponse.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("STORAGE", "Feil ved nedlasting: ${e.message}")
            null
        }
    }



    override suspend fun readTrajectory(file: File): IcebergTrajectoryData {

        val ncfile = NetcdfDatasets.openDataset(file.absolutePath)

        return try {

            fun readVar(name: String): List<Float> {
                val arr = ncfile.findVariable(name)?.read() ?: return emptyList()
                val out = mutableListOf<Float>()

                val it = arr.indexIterator
                while (it.hasNext()) {
                    out.add(it.floatNext)
                }
                return out
            }

            IcebergTrajectoryData(
                lat = readVar("lat"),
                lon = readVar("lon"),
                icebergXVelocity = readVar("iceb_x_velocity"),
                icebergYVelocity = readVar("iceb_y_velocity"),
                /* z = readVar("z"),
                ageSeconds = readVar("age_seconds"),
                status = readVar("status"),
                moving = readVar("moving"),
                sail = readVar("sail"),
                draft = readVar("draft"),
                length = readVar("length"),
                width = readVar("width"),
                icebergXVelocity = readVar("iceb_x_velocity"),
                icebergYVelocity = readVar("iceb_y_velocity"),
                xSeaWaterVelocity = readVar("x_sea_water_velocity"),
                ySeaWaterVelocity = readVar("y_sea_water_velocity"),
                xWind = readVar("x_wind"),
                yWind = readVar("y_wind"),
                waveHeight = readVar("sea_surface_wave_significant_height"),
                seaWaterTemperature = readVar("sea_water_temperature"),
                seaIceFraction = readVar("sea_ice_area_fraction"),

                 */
                time = readVar("time")
            )

        } finally {
            ncfile.close()
        }
    }

    override suspend fun loadSavedSimulations(): List<SavedSimulationUi> {
        val files = dir.listFiles() ?: return emptyList()
        return files
            .filter { it.extension == "nc" && it.name.startsWith("iceberg_") }
            .mapNotNull { ncFile ->
                try {
                    val uuid = ncFile.nameWithoutExtension.removePrefix("iceberg_")
                    val jsonFile = File(dir, "iceberg_$uuid.json")
                    if (!jsonFile.exists()) return@mapNotNull null

                    val response = Json.decodeFromString<DriftyIcebergSimulationResponse>(jsonFile.readText())

                    SavedSimulationUi(uuid, response, ncFile, jsonFile)
                } catch (_: Exception) { null }
            }
    }

}