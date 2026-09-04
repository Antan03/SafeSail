package no.uio.ifi.in2000.sofiaalo.team44

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.sofiaalo.team44.data.collisionRisk.CollisionRiskRepositoryImp
import no.uio.ifi.in2000.sofiaalo.team44.data.drifty.DriftyDataSource
import no.uio.ifi.in2000.sofiaalo.team44.data.drifty.DriftyRepositoryImp
import no.uio.ifi.in2000.sofiaalo.team44.data.fileStorage.FileStorageRepositoryImp
import no.uio.ifi.in2000.sofiaalo.team44.data.simulationStorage.SimulationStorageRepositoryImp
import no.uio.ifi.in2000.sofiaalo.team44.ui.components.IcebergPopUp
import no.uio.ifi.in2000.sofiaalo.team44.ui.drifty.DriftyViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.favorites.FavoritesViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.home.HomeViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.PlaybackViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.probability.ProbabilityViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.theme.Team44Theme
import no.uio.ifi.in2000.sofiaalo.team44.ui.tutorial.TutorialOverlay
import org.maplibre.android.MapLibre


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val username = "in2000_44"
        val password = "79qn33DR"
        val client = HttpClient(OkHttp) {
            install(ContentNegotiation) {

                json(Json { ignoreUnknownKeys = true

                })
            }
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = username , password = password)
                    }
                    sendWithoutRequest { true }
                }
            }
        }

        val fileStorage = FileStorageRepositoryImp(application)
        val dataSource = DriftyDataSource(client)
        val simulationStorageRepo = SimulationStorageRepositoryImp(fileStorage, client)
        val simulationRepo = DriftyRepositoryImp(dataSource)
        val riskRepository = CollisionRiskRepositoryImp()

        val driftyViewModel = DriftyViewModel(simulationRepo,simulationStorageRepo)
        val favoritesViewModel = FavoritesViewModel(simulationStorageRepo)
        val playbackViewModel = PlaybackViewModel(simulationStorageRepo,application)
        val probabilityViewModel = ProbabilityViewModel(riskRepository)

        MapLibre.getInstance(this)

        val homeViewModel = HomeViewModel(application)
        setContent {

            Team44Theme {

                val navController = rememberNavController()


                ScreenNavigation(navController,homeViewModel,favoritesViewModel,playbackViewModel,probabilityViewModel)
                IcebergPopUp(homeViewModel, driftyViewModel, favoritesViewModel, playbackViewModel)

                val hasSeen by homeViewModel.seen.collectAsState()

                AnimatedVisibility(
                    visible = !hasSeen,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TutorialOverlay(onDismiss = {
                        homeViewModel.tutorialComplete() }
                    ) }

            }
        }
    }
}