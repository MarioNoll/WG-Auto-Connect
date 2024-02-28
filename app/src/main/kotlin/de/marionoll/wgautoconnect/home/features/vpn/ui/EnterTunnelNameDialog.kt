package de.marionoll.wgautoconnect.home.features.vpn.ui

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.window.DialogProperties
import de.marionoll.wgautoconnect.R
import de.marionoll.wgautoconnect.home.Event
import de.marionoll.wgautoconnect.home.features.vpn.VPNViewState

@Composable
fun EnterTunnelNameDialog(
    viewState: VPNViewState.InputViewState,
    onEvent: (Event.VPN) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val windowInfo = LocalWindowInfo.current

    LaunchedEffect(windowInfo) {
        snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused) {
                focusRequester.requestFocus()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            onEvent(Event.VPN.Dialog.Dismiss)
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
        ),
        text = {
            TextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = viewState.input,
                keyboardActions = KeyboardActions(
                    onDone = {
                        onEvent(Event.VPN.Dialog.Confirm)
                    }
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                label = {
                    Text(
                        text = stringResource(id = R.string.tunnel_name_enter_dialog_label)
                    )
                },
                onValueChange = { value ->
                    onEvent(
                        Event.VPN.Dialog.Input(input = value)
                    )
                }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onEvent(Event.VPN.Dialog.Confirm)
                },
                enabled = viewState.confirmButtonEnabled,
            ) {
                Text(
                    text = stringResource(id = R.string.tunnel_name_enter_dialog_button_confirm)
                )
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onEvent(Event.VPN.Dialog.Dismiss)
                }
            ) {
                Text(
                    text = stringResource(id = R.string.general_button_cancel)
                )
            }
        }
    )
}
