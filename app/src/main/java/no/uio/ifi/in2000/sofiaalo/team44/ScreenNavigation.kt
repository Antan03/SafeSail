package no.uio.ifi.in2000.sofiaalo.team44

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.sofiaalo.team44.ui.components.MenuBar
import no.uio.ifi.in2000.sofiaalo.team44.ui.favorites.FavoritesScreen
import no.uio.ifi.in2000.sofiaalo.team44.ui.favorites.FavoritesViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.home.HomeScreen
import no.uio.ifi.in2000.sofiaalo.team44.ui.home.HomeViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.PlaybackViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.probability.ProbabilityViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.settings.SettingsScreen

@Composable
fun ScreenNavigation(
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel,
    favoritesViewModel: FavoritesViewModel,
    playbackViewModel: PlaybackViewModel,
    probabilityViewModel: ProbabilityViewModel
) {
    Scaffold(
        bottomBar = {
            MenuBar(navController = navController, onNavigateToFavorites = {navController.navigate("simulerte isfjell")})
        }
    ) { innerPadding ->


        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        )
        {

            composable("home") {
                HomeScreen(
                    modifier = Modifier,
                    homeViewModel = homeViewModel,
                    favoritesViewModel = favoritesViewModel,
                    playbackViewModel = playbackViewModel,
                    probabilityViewModel = probabilityViewModel
                )
            }
            composable("simulerte isfjell") {
                FavoritesScreen(
                    favoritesViewModel,
                    playbackViewModel
                )
            }
            composable("Instillinger"){
                SettingsScreen(playbackViewModel, homeViewModel, probabilityViewModel)
            }
        }
    }
}