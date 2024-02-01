package de.marionoll.wgautoconnect.home.features.vpn

import android.location.LocationManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.marionoll.wgautoconnect.data.AutoConnectState
import de.marionoll.wgautoconnect.data.SSID
import de.marionoll.wgautoconnect.data.Tunnel
import de.marionoll.wgautoconnect.home.Event
import de.marionoll.wgautoconnect.home.IntentNavigator
import de.marionoll.wgautoconnect.home.ui.Precondition
import de.marionoll.wgautoconnect.service.NETWORK_SERVICE_PERMISSIONS
import de.marionoll.wgautoconnect.service.NetworkMonitorServiceHandler
import de.marionoll.wgautoconnect.util.PermissionHelper
import de.marionoll.wgautoconnect.util.WireGuardAvailabilityProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class VPNViewModel
@Inject constructor(
    trustedNetworkDataStore: DataStore<SSID?>,
    private val autoConnectStateDataStore: DataStore<AutoConnectState?>,
    private val networkMonitorServiceHandler: NetworkMonitorServiceHandler,
    private val intentNavigator: IntentNavigator,
    private val locationManager: LocationManager,
    private val wireGuardAvailabilityProvider: WireGuardAvailabilityProvider,
    private val permissionHelper: PermissionHelper,
) : ViewModel() {

    private var handleAutoConnectClickJob: Job? = null
    private var enableVPNJob: Job? = null

    private val stateFlow = MutableStateFlow(
        VpnState(
            preConditionDialogType = null,
            input = null,
            requestNetworkPermissions = false,
        )
    )

    val viewState = combine(
        stateFlow,
        autoConnectStateDataStore.data,
        trustedNetworkDataStore.data
    ) { state, vpn, trustedWifi ->
        val inputViewState = state.input?.let { input ->
            VPNViewState.InputViewState(
                input = input,
                confirmButtonEnabled = input.text.isNotEmpty(),
            )
        }

        VPNViewState(
            vpnButtonEnabled = trustedWifi != null,
            isAutoConnectEnabled = vpn?.enabled == true,
            input = inputViewState,
            preConditionDialogType = state.preConditionDialogType,
            requestNetworkPermissions = state.requestNetworkPermissions,
        )
    }

    fun onEvent(event: Event.VPN) {
        when (event) {
            Event.VPN.Dialog.Confirm -> {
                onVPNDialogConfirm()
            }

            Event.VPN.Dialog.Dismiss -> {
                onVPNDialogDismiss()
            }

            is Event.VPN.Dialog.Input -> {
                onVPNDialogInput(input = event.input)
            }

            Event.VPN.ToggleVPNButtonClick -> {
                onVPNToggleStateRequest()
            }

            Event.VPN.PermissionDenied -> {
                showPreConditionDialog(
                    type = Precondition.Permission(
                        origin = Precondition.Origin.Monitor
                    )
                )
            }

            Event.VPN.PreconditionDialog.Dialog.Dismiss -> {
                showPreConditionDialog(type = null)
            }

            Event.VPN.PreconditionDialog.Dialog.Confirm -> {
                onPreconditionDialogConfirm()
            }

            Event.VPN.PermissionRequest -> {
                stateFlow.update { state ->
                    state.copy(
                        requestNetworkPermissions = false,
                    )
                }
            }
        }
    }

    private fun onPreconditionDialogConfirm() {
        stateFlow.value.preConditionDialogType?.let { preConditionDialogType ->
            when (preConditionDialogType) {
                is Precondition.Location -> intentNavigator.toLocationSettings()
                is Precondition.Permission -> intentNavigator.toAppSettings()
                is Precondition.WireGuard -> intentNavigator.toWireGuardPlay()
            }
        }
        showPreConditionDialog(type = null)
    }

    private fun onVPNToggleStateRequest() {
        handleAutoConnectClickJob?.cancel()
        handleAutoConnectClickJob = viewModelScope.launch {
            val vpn = autoConnectStateDataStore.data.first()
            if (vpn != null && vpn.enabled) {
                networkMonitorServiceHandler.stop()
            } else {
                requestVPNNameConfirmation(vpnName = vpn?.tunnel)
            }
        }
    }

    private fun onVPNDialogConfirm() {
        val input = stateFlow.value.input ?: return

        enableVPNJob?.cancel()
        enableVPNJob = viewModelScope.launch {
            autoConnectStateDataStore.updateData {
                AutoConnectState(
                    tunnel = Tunnel(input.text),
                    enabled = true,
                )
            }
            networkMonitorServiceHandler.start()
        }

        setInputState(input = null)
    }

    private fun onVPNDialogDismiss() {
        setInputState(input = null)
    }

    private fun onVPNDialogInput(input: TextFieldValue) {
        if (stateFlow.value.input != null) {
            setInputState(input = input)
        }
    }

    private fun requestVPNNameConfirmation(vpnName: Tunnel?) {
        if (!wireGuardAvailabilityProvider()) {
            showPreConditionDialog(
                type = Precondition.WireGuard(
                    origin = Precondition.Origin.Monitor
                )
            )
            return
        }

        if (!permissionHelper.hasPermissions(NETWORK_SERVICE_PERMISSIONS)) {
            stateFlow.update { state ->
                state.copy(
                    requestNetworkPermissions = true,
                )
            }
            return
        }


        if (!locationManager.isLocationEnabled) {
            showPreConditionDialog(
                type = Precondition.Location(
                    origin = Precondition.Origin.Monitor
                )
            )
            return
        }

        val text = vpnName?.value ?: ""
        setInputState(
            input = TextFieldValue(
                text = text,
                selection = TextRange(0, text.length)
            )
        )
    }

    private fun setInputState(input: TextFieldValue?) {
        stateFlow.update { state ->
            state.copy(
                input = input,
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
