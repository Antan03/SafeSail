package no.uio.ifi.in2000.sofiaalo.team44.ui.home


import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.uio.ifi.in2000.sofiaalo.team44.ui.components.DatePickerModal
import no.uio.ifi.in2000.sofiaalo.team44.ui.components.RiskPanel
import no.uio.ifi.in2000.sofiaalo.team44.ui.components.RouteInfoBottomSheet
import no.uio.ifi.in2000.sofiaalo.team44.ui.favorites.FavoritesViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.LayerControls
import no.uio.ifi.in2000.sofiaalo.team44.ui.map.MapView
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.PlaybackViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.probability.ProbabilityViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.tutorial.TutorialOverlay
import no.uio.ifi.in2000.sofiaalo.team44.util.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    homeViewModel: HomeViewModel,
    favoritesViewModel: FavoritesViewModel,
    playbackViewModel: PlaybackViewModel,
    probabilityViewModel: ProbabilityViewModel
) {
    val layers by homeViewModel.layers.collectAsStateWithLifecycle()
    val timestamp by homeViewModel.selectedTimestamp.collectAsStateWithLifecycle()
    val spilt = formatTimestamp(timestamp).split("T")
    val date = spilt[0]
    val route by homeViewModel.route.collectAsStateWithLifecycle()
    val wmsUrl =
        "https://victoria.met.no/wms?REQUEST=GetMap&SERVICE=WMS&VERSION=1.3.0&FORMAT=image%2Fpng&STYLES=icechart&TRANSPARENT=TRUE&TIME=${date}T00%3A00%3A00Z&LAYERS=Met_Norway_Ice_Chart&WIDTH=256&HEIGHT=256&CRS=EPSG%3A3857&BBOX={bbox-epsg-3857}"
    val currentIndex by playbackViewModel.globalTimeIndex.collectAsStateWithLifecycle()
    val activeIcebergs by playbackViewModel.activeIcebergs.collectAsStateWithLifecycle()
    val icebergState by homeViewModel.icebergUiState.collectAsStateWithLifecycle()
    val icebergs = when (val state = icebergState) {
        is UiState.Success -> state.data.icebergs.features
        else -> emptyList()
    }
    val metAlertsUiState by homeViewModel.metAlertsUiState.collectAsStateWithLifecycle()
    val metAlertsFeatures = when (val state = metAlertsUiState) {
        is UiState.Success -> state.data.alerts.features
        else -> emptyList()
    }
    val context = LocalContext.current
    val trajectories by playbackViewModel.trajectories.collectAsStateWithLifecycle()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    val hasSeen by homeViewModel.seen.collectAsState()


    LaunchedEffect(Unit) {
        favoritesViewModel.loadFavorites()
    }

    LaunchedEffect(activeIcebergs, trajectories, route) {
        probabilityViewModel.updateData(activeIcebergs, trajectories, route)
    }
    LaunchedEffect(Unit) {
        homeViewModel.userMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }


    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = 50.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {
            Box(modifier = Modifier.consumeWindowInsets(WindowInsets.navigationBars)) {
                HorizontalDivider(thickness = 2.dp, color = Color.Gray)
                RouteInfoBottomSheet(
                    currentIndex = currentIndex,
                    playbackViewModel = playbackViewModel,
                    homeViewModel = homeViewModel,
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MapView(
                wmsUrl = if (layers.showVictoriaLayer) wmsUrl else null,
                icebergFeatures = icebergs,
                metAlertsFeatures = metAlertsFeatures,
                route = route,
                currentIndex = currentIndex,
                onMapClick = { lat, lon -> homeViewModel.addWaypoint(lat, lon) },
                onIcebergClick = { lat, lon -> homeViewModel.selectIceberg(lat, lon) },
                playbackViewModel = playbackViewModel,
                layers = layers
            )

            // Øverst til venstre: lag-kontroller
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 8.dp, top = 8.dp)
            ) {
                LayerControls(
                    layers = layers,
                    onLayersChanged = { homeViewModel.updateLayers(it) }
                )
            }

            // Øverst til høyre: risikoanalyse
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 8.dp, top = 8.dp)
            ) {
                RiskPanel(
                    activeIcebergs = activeIcebergs,
                    probabilityViewModel = probabilityViewModel
                )
            }

            // Nederst til venstre: datepicker
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                DatePickerModal(
                    initialTimestampMillis = timestamp,
                    onTimestampSelected = { d, t -> homeViewModel.updateDate(d!!, t) }
                )
            }
            if(!hasSeen){
                TutorialOverlay(
                    onDismiss = {homeViewModel.tutorialComplete()}
                )
            }
        }
    }
}