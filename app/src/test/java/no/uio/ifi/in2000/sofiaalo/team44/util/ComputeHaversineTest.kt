package no.uio.ifi.in2000.sofiaalo.team44.util

import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.math.roundToInt

class ComputeHaversineTest {
    @Test
    fun testHaversine() {
        assertEquals(haversineDistance(50.0, 50.0, 51.0, 51.0).roundToInt(), 132)
    }
}