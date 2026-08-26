package de.marionoll.wgautoconnect.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.marionoll.wgautoconnect.home.features.network.ui.SelectNetwork
import de.marionoll.wgautoconnect.home.features.vpn.ui.EnterTunnelNameDialog
import de.marionoll.wgautoconnect.home.ui.Precondition
import de.marionoll.wgautoconnect.home.ui.PreconditionDialog

@Composable
fun HomeScreen(
    modifier: Modifier,
    viewState: HomeViewState.Content,
    onEvent: (Event) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    HomeScreenContent(
        modifier = modifier,
        viewState = viewState,
        onEvent = onEvent,
    )

    viewState.preconditionDialogType?.let { preconditionDialogType ->
        PreconditionDialog(
            onDismiss = {
                onEvent(
                    when (preconditionDialogType.origin) {
                        Precondition.Origin.Monitor -> Event.VPN.PreconditionDialog.Dialog.Dismiss
                        Precondition.Origin.Scan -> Event.Network.PreconditionDialog.Dialog.Dismiss
                    }
                )
            },
            onConfirm = {
                onEvent(
                    when (preconditionDialogType.origin) {
                        Precondition.Origin.Monitor -> Event.VPN.PreconditionDialog.Dialog.Confirm
                        Precondition.Origin.Scan -> Event.Network.PreconditionDialog.Dialog.Confirm
                    }
                )
            },
            type = preconditionDialogType,
        )
    }

    viewState.vpnViewState.input?.let { vpnViewState ->
        EnterTunnelNameDialog(
            viewState = vpnViewState,
            onEvent = onEvent,
        )
    }

    viewState.networkViewState.selectNetworkViewState?.wiFiState?.let { selectNetworkViewState ->
        ModalBottomSheet(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .navigationBarsPadding(),
            onDismissRequest = {
                onEvent(Event.Network.Sheet.Dismiss)
            },
            sheetState = sheetState,
            tonalElevation = 0.dp,
        ) {
            SelectNetwork(
                viewState = selectNetworkViewState,
                onEvent = onEvent,
            )
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationsPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) {}

        LaunchedEffect(Unit) {
            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private val HomeViewState.Content.preconditionDialogType: Precondition?
    get() = networkViewState.preConditionDialogType ?: vpnViewState.preConditionDialogType
