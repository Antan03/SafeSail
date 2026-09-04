package no.uio.ifi.in2000.sofiaalo.team44.data.metAlerts


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.sofiaalo.team44.model.metAlerts.MetAlerts
import no.uio.ifi.in2000.sofiaalo.team44.util.formatTimestamp


class MetAlertsDataSource {
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }

        install(HttpTimeout) {
            socketTimeoutMillis = 30000
        }
    }

    suspend fun getMetAlertsFromSource(timestamp: Long): MetAlerts {
        val timestamp = formatTimestamp(timestamp)
        val split = timestamp.split("T")
        var date = split[0]

        val split2 = formatTimestamp(System.currentTimeMillis()).split("T")
        val current = split2[0]

        date = if(date == current) "current.json?"
        else "archive.json?date=${date.take(10)}&"

        try {
            val response: MetAlerts =
                client.get("https://api.met.no/weatherapi/metalerts/2.0/${date}geographicDomain=marine").body()
            return response
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }
}
