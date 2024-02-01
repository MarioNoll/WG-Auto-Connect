package de.marionoll.wgautoconnect.home

import androidx.compose.ui.text.input.TextFieldValue
import de.marionoll.wgautoconnect.data.SSID

sealed interface Event {
    sealed interface VPN : Event {
        data object PermissionDenied : VPN
        data object PermissionRequest : VPN

        sealed interface PreconditionDialog : VPN {
            sealed interface Dialog : PreconditionDialog {
                data object Dismiss : Dialog
                data object Confirm : Dialog
            }
        }

        sealed interface Dialog : VPN {
            data object Confirm : Dialog
            data object Dismiss : Dialog
            data class Input(val input: TextFieldValue) : Dialog
        }

        data object ToggleVPNButtonClick : VPN
    }

    sealed interface Network : Event {
        data object PermissionDenied : Network

        sealed interface PreconditionDialog : Network {
            sealed interface Dialog : PreconditionDialog {
                data object Dismiss : Dialog
                data object Confirm : Dialog
            }
        }

        data object OnSelectClick : Network

        sealed interface Sheet : Network {
            data object Dismiss : Sheet
            data object PermissionGranted : Sheet
            data class Selected(val ssid: SSID) : Sheet
        }
    }

    data object WireGuardClick : Event
}
