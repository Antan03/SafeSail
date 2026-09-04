package no.uio.ifi.in2000.sofiaalo.team44.util

import java.math.BigDecimal
import java.math.RoundingMode

fun roundTwo(value: Double) : BigDecimal{
    val decimal = BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN)
    return decimal
}