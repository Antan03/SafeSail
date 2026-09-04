package no.uio.ifi.in2000.sofiaalo.team44.data.drifty

import io.ktor.client.call.body
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.DriftyIceBergSimulationRequest
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.DriftyIcebergSimulationResponse

class DriftyRepositoryImp(
    private val driftyDataSource: DriftyDataSource
) : DriftyRepository {

    override fun getSimulationResult(request: DriftyIceBergSimulationRequest): kotlinx.coroutines.flow.Flow<SimulationStatus> = flow {
        try {
            emit(SimulationStatus.Starting)

            val httpResponse = driftyDataSource.postSimulation(request)

            val statusUrl = httpResponse.headers[HttpHeaders.Location]

            if (statusUrl == null) {
                emit(SimulationStatus.Error("Mangler Location"))
                return@flow
            }

            val nextWaitInterval =
                httpResponse.headers[HttpHeaders.RetryAfter]?.toIntOrNull() ?: 10

            var totalSeconds = 0
            var attempts = 0

            while (attempts < 25) {

                repeat(nextWaitInterval) {
                    emit(SimulationStatus.Waiting(totalSeconds))
                    delay(1000L)
                    totalSeconds++
                }

                attempts++

                val statusResponse = driftyDataSource.getStatus(statusUrl)

                if (statusResponse.status == HttpStatusCode.OK) {
                    val body = statusResponse.body<DriftyIcebergSimulationResponse>()
                    emit(SimulationStatus.Success(body, totalSeconds))
                    return@flow
                }
            }

            emit(SimulationStatus.Error("Timeout - ingen respons fra server")) // bytter state

        } catch (e: Exception) {
            emit(SimulationStatus.Error(e.message ?: "Ukjent feil")) // state
        }
    }

}