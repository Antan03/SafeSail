
package no.uio.ifi.in2000.sofiaalo.team44.ui.home

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.sofiaalo.team44.data.iceberg.IcebergRepository
import no.uio.ifi.in2000.sofiaalo.team44.data.iceberg.IcebergRepositoryImp
import no.uio.ifi.in2000.sofiaalo.team44.data.metAlerts.MetAlertsRepository
import no.uio.ifi.in2000.sofiaalo.team44.data.metAlerts.MetAlertsRepositoryImp
import no.uio.ifi.in2000.sofiaalo.team44.data.tutorial.TutorialRepository
import no.uio.ifi.in2000.sofiaalo.team44.model.iceberg.Feature
import no.uio.ifi.in2000.sofiaalo.team44.model.iceberg.Icebergs
import no.uio.ifi.in2000.sofiaalo.team44.model.map.MapLayers
import no.uio.ifi.in2000.sofiaalo.team44.model.metAlerts.MetAlerts
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.ShipRoute
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.Waypoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.abs

data class IcebergUiState(
    val icebergs: Icebergs,
    val selectedIceberg: Feature? = null
)
data class MetAlertsUiState(
    val alerts: MetAlerts
)

class HomeViewModel(application: Application) : AndroidViewModel(application){
    private var _selectedTimestamp = MutableStateFlow(System.currentTimeMillis())
    val selectedTimestamp = _selectedTimestamp.asStateFlow()

    private val icebergRepository: IcebergRepository = IcebergRepositoryImp()
    private val metAlertsRepository: MetAlertsRepository = MetAlertsRepositoryImp()


    private val _icebergUiState = MutableStateFlow<UiState<IcebergUiState>>(UiState.Idle)
    val icebergUiState = _icebergUiState.asStateFlow()
    private val _metAlertsUiState = MutableStateFlow<UiState<MetAlertsUiState>>(UiState.Idle)
    val metAlertsUiState = _metAlertsUiState.asStateFlow()
    private val _speedKnots = MutableStateFlow(12.0)
    val speedKnots = _speedKnots.asStateFlow()

    private val _layers = MutableStateFlow(MapLayers())
    val layers = _layers.asStateFlow()

    fun updateLayers(layers: MapLayers) { _layers.value = layers }

    private val _route = MutableStateFlow(ShipRoute())
    val route = _route.asStateFlow()

    private val tutorialRepository = TutorialRepository(application)
    val seen : StateFlow<Boolean> = tutorialRepository.showTutorial.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun tutorialComplete(){
        viewModelScope.launch {
            tutorialRepository.setTutorialCompleted(true)
        }
    }
    fun startTutorial(){
        viewModelScope.launch {
            tutorialRepository.setTutorialCompleted(false)
        }
    }

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    fun addWaypoint(lat: Double, lon: Double) {
        _route.update { it.withWaypoint(Waypoint(lat, lon)) }
    }
    fun removeWaypoint(point: Waypoint) {
        _route.value = _route.value.removeWayPoint(point)
    }

    fun setSpeed(knots: Double) {
        _speedKnots.value = knots
        // Oppdater ruten med ny hastighet
        _route.update { it.copy(defaultSpeedKnots = knots) }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun updateDate(dateMillis: Long, time: TimePickerState) {
        val zone = ZoneId.systemDefault()

        val localDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        val localDateTime = LocalDateTime.of(
            localDate,
            LocalTime.of(time.hour, time.minute)
        )

        val newTimestamp = localDateTime
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        _selectedTimestamp.value = newTimestamp
        _route.update { it.copy(startTime = newTimestamp) }

        getIcebergs(dateMillis)
        getMetAlerts(dateMillis)
    }

    val timeSteps: StateFlow<List<Long>> = combine(_route, _selectedTimestamp) { route, timestamp ->
        if (route.waypoints.size < 2) listOf(timestamp)
        else route.buildTimeSteps(timestamp)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(System.currentTimeMillis())
    )


    fun getIcebergs(timestamp:Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _icebergUiState.value = UiState.Loading
            try {
                val icebergs = icebergRepository.getIcebergsFromSource(timestamp)
                _icebergUiState.value = UiState.Success(IcebergUiState(icebergs))
            }
            catch (e: Exception) {
                _icebergUiState.value = UiState.Error(e)
                val text = if ((e.message ?: "").contains("ingen isfjell-data"))
                    "Ingen isfjelldata funnet for valgt dato. Prøv å endre dato."
                else
                    "Kunne ikke laste isfjelldata. Sjekk internettforbindelsen."
                _userMessage.emit(text)
            }
        }
    }
    fun getMetAlerts(timestamp: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _metAlertsUiState.value = UiState.Loading
            try {
                val alerts = metAlertsRepository.getMetAlertsFromSource(timestamp)
                _metAlertsUiState.value = UiState.Success(MetAlertsUiState(alerts))
            } catch (e: Exception) {
                _metAlertsUiState.value = UiState.Error(e)
            }
        }
    }
    fun selectIceberg(lat: Double, lon: Double){
        val currState = _icebergUiState.value
        if (currState is UiState.Success) {
            val feature = currState.data.icebergs.features.find {
                // sjekker om punktet gitt er nærme et isfjell
                val delLat = abs(it.geometry.coordinates[1] - lat)
                val delLon = abs(it.geometry.coordinates[0] - lon)
                delLat < 0.1 && delLon < 0.1
            }
            if (feature != null) {
                _icebergUiState.value = UiState.Success(
                    currState.data.copy(selectedIceberg = feature)
                )
            }
        }
    }

    fun clearSelect() {
        val currState = _icebergUiState.value
        if ( currState is UiState.Success){
            _icebergUiState.value = UiState.Success(
                currState.data.copy(selectedIceberg = null)
            )
        }
    }

    init {
            getIcebergs(_selectedTimestamp.value)
            getMetAlerts(_selectedTimestamp.value)
        }
    }

