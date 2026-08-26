package de.marionoll.wgautoconnect.home.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.marionoll.wgautoconnect.R
import de.marionoll.wgautoconnect.home.Event
import de.marionoll.wgautoconnect.home.HomeViewState
import de.marionoll.wgautoconnect.home.features.vpn.VPNViewState
import de.marionoll.wgautoconnect.service.NETWORK_SERVICE_PERMISSIONS

@Composable
fun AutoConnect(
    viewState: HomeViewState.Content,
    onEvent: (Event) -> Unit,
) {
    val vpnViewState = viewState.vpnViewState

    val networkScanPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val allGranted = result.all { (_, granted) -> granted }
        onEvent(
            if (allGranted) {
                Event.VPN.ToggleVPNButtonClick
            } else {
                Event.VPN.PermissionDenied
            }
        )
    }

    if (vpnViewState.requestNetworkPermissions) {
        LaunchedEffect(Unit) {
            networkScanPermissionsLauncher.launch(NETWORK_SERVICE_PERMISSIONS.toTypedArray())
            onEvent(Event.VPN.PermissionRequest)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HomeButton(
            style = HomeButtonStyle.AutoConnect,
            enabled = vpnViewState.vpnButtonEnabled,
            text = stringResource(id = vpnViewState.connectButtonText),
            onClick = { onEvent(Event.VPN.ToggleVPNButtonClick) },
        )

        Spacer(modifier = Modifier.size(8.dp))

        val trustedNetwork = viewState.networkViewState.trustedNetwork
        if (trustedNetwork != null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(id = vpnViewState.statusText),
                fontStyle = FontStyle.Italic,
            )
        }
    }
}

private val VPNViewState.connectButtonText: Int
    @StringRes
    get() = if (isAutoConnectEnabled) {
        R.string.home_button_connect_disable
    } else {
        R.string.home_button_connect_enable
    }

private val VPNViewState.statusText: Int
    @StringRes
    get() = if (isAutoConnectEnabled) {
        R.string.home_connect_status_enabled
    } else {
        R.string.home_connect_status_disabled
    }