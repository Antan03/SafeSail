package no.uio.ifi.in2000.sofiaalo.team44.ui.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.Waypoint
import no.uio.ifi.in2000.sofiaalo.team44.util.formatTimestamp
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment.application

@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var homeViewModel: HomeViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        // Sett opp test-dispatcher for Main
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        application = ApplicationProvider.getApplicationContext()
        homeViewModel = HomeViewModel(application)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun setSpeed() {
        homeViewModel.setSpeed(20.0)
        assertEquals(20.0, homeViewModel.speedKnots.value, 0.0)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun updateDate() {
        val time: TimePickerState = TimePickerState(20, 20, true)
        homeViewModel.updateDate(1588809600000, time)

        assertEquals(
            formatTimestamp(1588875600000),
            formatTimestamp(homeViewModel.selectedTimestamp.value)
        )
    }

    @Test
    fun addWaypoint() {
        homeViewModel.addWaypoint(50.0, 50.0)
        assertEquals(Waypoint(50.0, 50.0), homeViewModel.route.value.waypoints[0])
        homeViewModel.addWaypoint(51.0, 51.0)
        assertEquals(Waypoint(51.0, 51.0), homeViewModel.route.value.waypoints[1])
        homeViewModel.addWaypoint(52.0, 52.0)
        assertEquals(Waypoint(52.0, 52.0), homeViewModel.route.value.waypoints[2])
    }

    @Test
    fun removeWaypoint() {
        homeViewModel.addWaypoint(50.0, 50.0)
        homeViewModel.addWaypoint(51.0, 51.0)
        homeViewModel.addWaypoint(52.0, 52.0)

        homeViewModel.removeWaypoint(Waypoint(52.0, 52.0))
        homeViewModel.addWaypoint(52.1, 52.1)

        assertEquals(Waypoint(52.1, 52.1), homeViewModel.route.value.waypoints[2])
    }
}