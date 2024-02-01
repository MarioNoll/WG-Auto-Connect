package de.marionoll.wgautoconnect.home.features.vpn

import androidx.compose.ui.text.input.TextFieldValue
import de.marionoll.wgautoconnect.home.ui.Precondition

data class VpnState(
    val preConditionDialogType: Precondition?,
    val input: TextFieldValue?,
    val requestNetworkPermissions: Boolean,
)
