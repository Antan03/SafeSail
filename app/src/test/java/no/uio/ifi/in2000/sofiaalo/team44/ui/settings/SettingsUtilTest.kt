package no.uio.ifi.in2000.sofiaalo.team44.ui.settings

import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment.application

@RunWith(RobolectricTestRunner::class)
class SettingsUtilTest {
    private val dispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        // Sett opp test-dispatcher for Main
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        application = ApplicationProvider.getApplicationContext()
    }
    @Test
    fun setAnimationDelay() {
        SettingsUtil.setAnimationDelay(application, 400)
        assertEquals(400, SettingsUtil.getAnimationDelay(application))
    }

    @Test
    fun setParticleCount() {
        SettingsUtil.setParticleCount(application, 900)
        assertEquals(900, SettingsUtil.getParticleCount(application))
    }

    @Test
    fun setLineOpacity() {
        SettingsUtil.setLineOpacity(application, 0.07f)
        assertEquals(0.07f, SettingsUtil.getLineOpacity(application))
    }

}