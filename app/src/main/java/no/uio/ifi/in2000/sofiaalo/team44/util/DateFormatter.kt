package no.uio.ifi.in2000.sofiaalo.team44.util

import com.squareup.wire.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatTimestamp(timestamp: Long): String {
    try {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ISO_INSTANT
            .withZone(ZoneId.of("UTC"))
        val formatted = formatter.format(instant)
        //formatted = "yyyy-MM-ddThh:mm:ss.sssZ"
        return formatted
    }
    catch (e: Exception){
        throw Exception("Error: ${e.message}")

    }
}
fun subtractDays(date: String, numberOfDays: Int) : String{
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    val parsedDate = LocalDate.parse(date, formatter)

    val newDate = parsedDate.minusDays(numberOfDays.toLong())

    return newDate.format(formatter)
}
