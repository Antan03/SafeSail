package no.uio.ifi.in2000.sofiaalo.team44.util

import junit.framework.TestCase.assertEquals
import org.junit.Test

class DateFormatterTest {
    @Test
    fun formatTimestamp() {
        assertEquals(
            "2026-02-27T14:15:00Z",
            formatTimestamp(1772201700000)
        )
    }

    @Test
    fun subtractDays() {
        assertEquals(
            "2026-05-11",
            subtractDays("2026-05-16", 5)
        )
    }
}