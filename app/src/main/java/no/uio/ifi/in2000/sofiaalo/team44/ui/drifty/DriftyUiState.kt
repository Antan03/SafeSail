package no.uio.ifi.in2000.sofiaalo.team44.ui.drifty

import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.DriftyIcebergSimulationResponse

// tilstander som skjermen kan være i skal være definert her, bare en kan være sann samtidig
@Suppress("unused")
sealed class UiState {
    object Idle : UiState()
    data class Success(val data: DriftyIcebergSimulationResponse) : UiState()
    data class Error(val message: String = "Ukjent feil") : UiState()}

