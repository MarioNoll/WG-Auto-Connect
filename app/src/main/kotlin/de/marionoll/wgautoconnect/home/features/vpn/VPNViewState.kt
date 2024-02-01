package de.marionoll.wgautoconnect.home.features.vpn

import androidx.compose.ui.text.input.TextFieldValue
import de.marionoll.wgautoconnect.home.ui.Precondition

data class VPNViewState(
    val isAutoConnectEnabled: Boolean,
    val vpnButtonEnabled: Boolean,
    val input: InputViewState?,
    val preConditionDialogType: Precondition?,
    val requestNetworkPermissions: Boolean,
) {
    data class InputViewState(
        val input: TextFieldValue,
        val confirmButtonEnabled: Boolean,
    )
}