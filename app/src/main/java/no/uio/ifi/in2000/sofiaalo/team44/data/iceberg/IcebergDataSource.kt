package no.uio.ifi.in2000.sofiaalo.team44.data.iceberg

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.sofiaalo.team44.model.iceberg.Icebergs
import no.uio.ifi.in2000.sofiaalo.team44.util.formatTimestamp
import no.uio.ifi.in2000.sofiaalo.team44.util.subtractDays

class IcebergDataSource {
    //Initialize client
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

    suspend fun getIcebergsInfoFromSource(timestamp: Long): Icebergs {
        val timestamp = formatTimestamp(timestamp)
        val split = timestamp.split("T")
        var date = split[0]





        //Get API response
        val maxAttempts = 10
        var attempt = 0
        while (attempt < maxAttempts){

            try {
                val response: HttpResponse = client.get("https://api.met.no/weatherapi/iceberg/0.1/?date=$date")

                // 1. Sjekk om status er 200 OK før vi prøver å lese JSON
                if (response.status.value in 200..299) {
                    val data: Icebergs = response.body()

                    // 2. Sjekk om vi faktisk fikk isfjell
                    if (data.features.isNotEmpty()) {
                        return data
                    }
                }
            } catch (e: Exception) {
                throw Exception("Error: ${e.message}")
            }
            date = subtractDays(date, 5) // går tilbake i tid med 5 dager, tar hensyn til måneder
            attempt++
        }
        throw Exception("Fant ingen isfjell-data etter $maxAttempts forsøk.")
    }
}