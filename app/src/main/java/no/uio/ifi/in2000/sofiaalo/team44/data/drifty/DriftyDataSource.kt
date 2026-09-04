package no.uio.ifi.in2000.sofiaalo.team44.data.drifty

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.DriftyIceBergSimulationRequest

// Snakker bare med drifty api-et
class DriftyDataSource(private val client: HttpClient) {
    private val baseUrl = "https://in2000.drifty.met.no"


    suspend fun postSimulation(request: DriftyIceBergSimulationRequest): HttpResponse {
        return client.post("$baseUrl/api/openberg/v1") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getStatus(statusUrl: String): HttpResponse {
        val fullUrl = if (statusUrl.startsWith("http")) statusUrl else "$baseUrl$statusUrl"
        return client.get(fullUrl)
    }
}
