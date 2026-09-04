package no.uio.ifi.in2000.sofiaalo.team44.ui.playback

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import no.uio.ifi.in2000.sofiaalo.team44.data.simulationStorage.SimulationStorageRepository
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.IcebergTrajectoryData
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.SavedSimulationUi
import no.uio.ifi.in2000.sofiaalo.team44.model.ship.ShipRoute
import no.uio.ifi.in2000.sofiaalo.team44.ui.settings.SettingsUtil
import java.time.Instant

enum class SliderMode {
    SHIP_ROUTE,   // Tid styres av skipet. Simuleringer vises kun når de er relevante.
    SIMULATION    // Tid styres av simulering. Skip og rute skjules.
}

class PlaybackViewModel(
    private val storageRepository: SimulationStorageRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _icebergs = MutableStateFlow<List<SavedSimulationUi>>(emptyList())
    private val _trajectories = MutableStateFlow<Map<String, IcebergTrajectoryData>>(emptyMap())
    val trajectories: StateFlow<Map<String, IcebergTrajectoryData>> = _trajectories.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    private val _globalTimeIndex = MutableStateFlow(0)
    val globalTimeIndex: StateFlow<Int> = _globalTimeIndex.asStateFlow()

    private val _sliderMode = MutableStateFlow(SliderMode.SHIP_ROUTE)
    val sliderMode: StateFlow<SliderMode> = _sliderMode.asStateFlow()

    private val _currentRoute = MutableStateFlow(ShipRoute())

    private val _heavyRendering = MutableStateFlow(false)
    private val _particleCount  = MutableStateFlow(1000)
    val particleCount: StateFlow<Int>   = _particleCount.asStateFlow()
    private val _animationDelay = MutableStateFlow(500)
    private val _lineOpacity    = MutableStateFlow(0.08f)
    val lineOpacity: StateFlow<Float>   = _lineOpacity.asStateFlow()


    val activeIcebergs: StateFlow<List<SavedSimulationUi>> = combine(
        _icebergs, _selectedIds
    ) { all, selected ->
        all.filter { it.uuid in selected }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val visibleIcebergs: StateFlow<List<SavedSimulationUi>> = combine(
        activeIcebergs, _globalTimeIndex, _sliderMode, _currentRoute, _trajectories
    ) { active, index, mode, _, _ ->
        when (mode) {
            SliderMode.SIMULATION -> active
            SliderMode.SHIP_ROUTE -> active.filter { iceberg ->
                resolveSimulationIndex(index, iceberg) != null
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val maxTotalHours: StateFlow<Int> = _particleCount
        .map { p -> (1_000 * 120 / p).coerceIn(1, 999) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 120)

    val usedHours: StateFlow<Int> = combine(_selectedIds, _icebergs) { selected, all ->
        selected.sumOf { id ->
            all.find { it.uuid == id }?.response?.spec?.simulationDurationInHours ?: 0
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    private var animationJob: Job? = null

    init {
        val ctx = application.applicationContext
        _particleCount.value  = SettingsUtil.getParticleCount(ctx)
        _animationDelay.value = SettingsUtil.getAnimationDelay(ctx)
        _lineOpacity.value    = SettingsUtil.getLineOpacity(ctx)

        viewModelScope.launch(Dispatchers.IO) {
            _icebergs.value = storageRepository.loadSavedSimulations()
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _icebergs.value = storageRepository.loadSavedSimulations()
        }
    }

    fun updateRoute(route: ShipRoute) {
        _currentRoute.value = route
    }

    fun setMode(mode: SliderMode) {
        _sliderMode.value = mode
        resetAnimation()
    }

    fun toggleIceberg(uuid: String) {
        val nowSelected = uuid !in _selectedIds.value

        if (nowSelected) {
            val budget = 1_000 * 120
            val activeCost = _selectedIds.value.sumOf { id ->
                val hours = _icebergs.value.find { it.uuid == id }
                    ?.response?.spec?.simulationDurationInHours ?: 24
                hours * _particleCount.value
            }
            val newHours = _icebergs.value.find { it.uuid == uuid }
                ?.response?.spec?.simulationDurationInHours ?: 24
            val newCost = newHours * _particleCount.value
            if (activeCost + newCost > budget) {
                _capacityMessage.tryEmit(
                    "For tung å animere med nåværende innstillinger. " +
                    "Deaktiver en simulering eller reduser partikler."
                )
                return
            }
        }

        _selectedIds.update { current ->
            if (nowSelected) current + uuid else current - uuid
        }

        if (nowSelected && _trajectories.value[uuid] == null) {
            viewModelScope.launch(Dispatchers.IO) {
                // Isfjelldata mangler om bruker trykker kortet før refresh() er ferdig
                // (f.eks. rett etter at en ny simulering ble lagret). Last inn på nytt.
                var iceberg = _icebergs.value.find { it.uuid == uuid }
                if (iceberg == null) {
                    _icebergs.value = storageRepository.loadSavedSimulations()
                    iceberg = _icebergs.value.find { it.uuid == uuid } ?: return@launch
                }
                val trajectory = storageRepository.readTrajectory(iceberg.ncFile)
                _trajectories.update { it + (uuid to trajectory) }
            }
        }
    }

    fun resolveSimulationIndex(
        globalIndex: Int,
        iceberg: SavedSimulationUi
    ): Int? {
        return when (_sliderMode.value) {
            SliderMode.SIMULATION -> {
                val traj = _trajectories.value[iceberg.uuid] ?: return null
                if (globalIndex < 0 || globalIndex >= traj.time.size) null else globalIndex
            }

            SliderMode.SHIP_ROUTE -> {
                val shipTimeMs = _currentRoute.value.startTime + (globalIndex * 3_600_000L)
                val icebergStartMs = runCatching {
                    Instant.parse(iceberg.response.spec.geo.point.time).toEpochMilli()
                }.getOrNull() ?: return null

                val simIndex = ((shipTimeMs - icebergStartMs) / 3_600_000L).toInt()
                val traj = _trajectories.value[iceberg.uuid] ?: return null

                if (simIndex < 0 || simIndex >= traj.time.size) null else simIndex
            }
        }
    }

    // ── Animasjon ──────────────────────────────────────────────────────────────

    private var _lastMaxIndex: Int = 0
    private var _wasPausedByCamera: Boolean = false

    fun pauseForCamera() {
        if (animationJob?.isActive == true) {
            _wasPausedByCamera = true
            animationJob?.cancel()
            _heavyRendering.value = false
        }
    }

    fun resumeAfterCamera() {
        if (_wasPausedByCamera) {
            _wasPausedByCamera = false
            startAnimation(_lastMaxIndex)
        }
    }

    fun startAnimation(maxIndex: Int) {
        _lastMaxIndex = maxIndex
        if (animationJob?.isActive == true) return
        if (_globalTimeIndex.value >= maxIndex) _globalTimeIndex.value = 0

        animationJob = viewModelScope.launch {
            _heavyRendering.value = true

            // Vent på at data lastes (samme som før)
            withTimeoutOrNull(10_000L) {
                while (!_trajectories.value.keys.containsAll(_selectedIds.value)) {
                    delay(100)
                }
            }

            while (_globalTimeIndex.value < maxIndex) {
                delay(_animationDelay.value.toLong())
                _globalTimeIndex.value += 1
            }

            _heavyRendering.value = false
        }
    }

    fun stopAnimation() {
        animationJob?.cancel()
        _heavyRendering.value = false
    }

    fun resetAnimation() {
        stopAnimation()
        _globalTimeIndex.value = 0
        _heavyRendering.value = false
    }

    fun seekTo(index: Int) {
        _globalTimeIndex.value = index
    }
    fun setLineOpacity(opacity: Float) {
        _lineOpacity.value = opacity
    }

    private val _capacityMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val capacityMessage = _capacityMessage.asSharedFlow()

    private val _showTrajectoryLines = MutableStateFlow(true)
    val showTrajectoryLines: StateFlow<Boolean> = _showTrajectoryLines.asStateFlow()

    fun toggleTrajectoryLines() {
        _showTrajectoryLines.value = !_showTrajectoryLines.value
    }

    fun setParticleCount(count: Int) {
        _particleCount.value = count
    }

    fun setAnimationDelay(ms: Int) {
        _animationDelay.value = ms
    }
}