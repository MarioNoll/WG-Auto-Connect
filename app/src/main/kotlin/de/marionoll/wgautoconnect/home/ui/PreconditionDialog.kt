package de.marionoll.wgautoconnect.home.ui

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import de.marionoll.wgautoconnect.R

@Composable
fun PreconditionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    type: Precondition,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
        ),
        text = {
            Text(
                text = stringResource(id = type.description),
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
            ) {
                Text(
                    text = stringResource(id = type.confirmButtonText),
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
            ) {
                Text(
                    text = stringResource(id = R.string.general_button_cancel),
                )
            }
        }
    )
}

sealed interface Precondition {

    val origin: Origin

    data class Permission(override val origin: Origin) : Precondition
    data class Location(override val origin: Origin) : Precondition
    data class WireGuard(override val origin: Origin) : Precondition

    sealed interface Origin {
        data object Scan : Origin
        data object Monitor : Origin
    }
}

private val Precondition.confirmButtonText: Int
    @StringRes get() = when (this) {
        is Precondition.Permission -> R.string.general_button_settings
        is Precondition.Location -> R.string.general_button_enable
        is Precondition.WireGuard -> R.string.general_button_install
    }

private val Precondition.description: Int
    @StringRes get() = when (this) {
        is Precondition.Location -> {
            R.string.location_dialog_description
        }

        is Precondition.Permission -> {
            when (origin) {
                Precondition.Origin.Monitor -> R.string.permission_dialog_monitor_description
                Precondition.Origin.Scan -> R.string.permission_dialog_scan_description
            }
        }

        is Precondition.WireGuard -> R.string.permission_dialog_wire_guard_description
    }