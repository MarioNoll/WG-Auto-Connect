package de.marionoll.wgautoconnect.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.rememberPermissionState
import de.marionoll.wgautoconnect.R
import de.marionoll.wgautoconnect.home.Event
import de.marionoll.wgautoconnect.home.features.network.NetworkViewState
import de.marionoll.wgautoconnect.home.features.network.scan.NETWORK_SCAN_PERMISSION

@Composable
fun SelectNetwork(
    networkViewState: NetworkViewState,
    onEvent: (Event) -> Unit,
) {
    val networkScanPermissionState = rememberPermissionState(
        NETWORK_SCAN_PERMISSION,
        onPermissionResult = { permissionGranted ->
            onEvent(
                if (permissionGranted) {
                    Event.Network.OnSelectClick
                } else {
                    Event.Network.PermissionDenied
                }
            )
        }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HomeButton(
            style = HomeButtonStyle.Select,
            text = stringResource(id = R.string.home_button_select_network),
            onClick = networkScanPermissionState::launchPermissionRequest,
        )

        Spacer(modifier = Modifier.size(8.dp))

        AnimatedContent(
            targetState = networkViewState.trustedNetwork,
            label = "trusted_network_animation"
        ) { trustedNetwork ->
            if (trustedNetwork != null) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(
                        id = R.string.home_selected_network,
                        trustedNetwork.value
                    ),
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}
