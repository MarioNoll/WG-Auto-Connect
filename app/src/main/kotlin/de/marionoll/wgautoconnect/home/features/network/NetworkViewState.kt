package de.marionoll.wgautoconnect.home.features.network

import de.marionoll.wgautoconnect.data.SSID
import de.marionoll.wgautoconnect.home.ui.Precondition

data class NetworkViewState(
    val selectNetworkViewState: NetworkState?,
    val trustedNetwork: SSID?,
    val preConditionDialogType: Precondition?,
)
