package de.marionoll.wgautoconnect.home.features.network

import de.marionoll.wgautoconnect.home.features.network.scan.Network
import de.marionoll.wgautoconnect.home.ui.Precondition


data class NetworkState(
    val preConditionDialogType: Precondition?,
    val wiFiState: WiFi?,
) {

    sealed interface WiFi {
        data object Loading : WiFi
        data class Content(val items: List<Network>) : WiFi
        data object Empty : WiFi
    }
}
