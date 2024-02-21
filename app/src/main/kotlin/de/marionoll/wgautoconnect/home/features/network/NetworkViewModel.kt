package de.marionoll.wgautoconnect.home.features.network

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.marionoll.wgautoconnect.data.SSID
import de.marionoll.wgautoconnect.home.Event
import de.marionoll.wgautoconnect.home.IntentNavigator
import de.marionoll.wgautoconnect.home.features.network.scan.NetworkScanResult
import de.marionoll.wgautoconnect.home.features.network.scan.NetworkScanner
import de.marionoll.wgautoconnect.home.ui.Precondition
import de.marionoll.wgautoconnect.service.NetworkMonitorServiceHandler
import de.marionoll.wgautoconnect.util.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class NetworkViewModel
@Inject constructor(
    private val trustedNetworkDataStore: DataStore<SSID?>,
    private val networkScanner: NetworkScanner,
    private val locationHelper: LocationHelper,
    private val intentNavigator: IntentNavigator,
    private val serviceHandler: NetworkMonitorServiceHandler,
) : ViewModel() {

    private var scanNetworksJob: Job? = null
    private var updateSSIDJob: Job? = null

    private val stateFlow = MutableStateFlow(
        NetworkState(
            preConditionDialogType = null,
            wiFiState = null,
        )
    )

    val viewState = combine(stateFlow, trustedNetworkDataStore.data) { state, trustedNetwork ->
        NetworkViewState(
            selectNetworkViewState = state,
            trustedNetwork = trustedNetwork,
            preConditionDialogType = state.preConditionDialogType,
        )
    }

    fun onEvent(event: Event.Network) {
        when (event) {
            Event.Network.OnSelectClick,
            Event.Network.Sheet.PermissionGranted -> {
                onSelectNetwork()
            }

            Event.Network.Sheet.Dismiss -> {
                onSelectNetworkDismiss()
            }

            is Event.Network.Sheet.Selected -> {
                onNetworkSelected(ssid = event.ssid)
            }

            Event.Network.PreconditionDialog.Dialog.Dismiss -> {
                showPreConditionDialog(type = null)
            }

            Event.Network.PreconditionDialog.Dialog.Confirm -> {
                onPreconditionDialogConfirm()
            }

            Event.Network.PermissionDenied -> {
                showPreConditionDialog(
                    type = Precondition.Permission(
                        origin = Precondition.Origin.Scan
                    )
                )
            }
        }
    }

    private fun onPreconditionDialogConfirm() {
        stateFlow.value.preConditionDialogType?.let { preConditionDialogType ->
            when (preConditionDialogType) {
                is Precondition.Permission -> intentNavigator.toAppSettings()
                is Precondition.Location -> intentNavigator.toLocationSettings()
                is Precondition.WireGuard -> intentNavigator.toWireGuardPlay()
            }
        }
        showPreConditionDialog(type = null)
    }

    private fun onSelectNetwork() {
        scanNetworksJob?.cancel()

        if (!locationHelper.isLocationEnabled) {
            showPreConditionDialog(
                type = Precondition.Location(
                    origin = Precondition.Origin.Scan
                )
            )
            return
        }

        setWiFiState(wiFiState = NetworkState.WiFi.Loading)
        scanNetworksJob = viewModelScope.launch {
            networkScanner.scan().collectLatest { scanResult ->
                setWiFiState(
                    wiFiState = scanResult.toWiFiState(),
                )
            }
        }
    }

    private fun NetworkScanResult.toWiFiState(): NetworkState.WiFi? {
        return when (this) {
            is NetworkScanResult.Content -> {
                if (networks.isEmpty()) {
                    NetworkState.WiFi.Empty
                } else {
                    NetworkState.WiFi.Content(
                        items = networks,
                    )
                }
            }

            is NetworkScanResult.PermissionMissing -> null
        }
    }

    private fun onSelectNetworkDismiss() {
        scanNetworksJob?.cancel()
        setWiFiState(wiFiState = null)
    }

    private fun onNetworkSelected(ssid: SSID) {
        updateSSIDJob?.cancel()
        updateSSIDJob = viewModelScope.launch {
            serviceHandler.stop()
            trustedNetworkDataStore.updateData { ssid }
        }

        setWiFiState(wiFiState = null)
    }

    private fun setWiFiState(wiFiState: NetworkState.WiFi?) {
        stateFlow.update { state ->
            state.copy(
                wiFiState = wiFiState,
            )
        }
    }

    private fun showPreConditionDialog(type: Precondition?) {
        stateFlow.update { state ->
            state.copy(
                preConditionDialogType = type
            )
        }
    }
}