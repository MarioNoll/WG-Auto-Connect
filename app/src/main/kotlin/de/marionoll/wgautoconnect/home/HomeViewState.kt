package de.marionoll.wgautoconnect.home

import de.marionoll.wgautoconnect.home.features.network.NetworkViewState
import de.marionoll.wgautoconnect.home.features.vpn.VPNViewState

sealed interface HomeViewState {
    data object Loading : HomeViewState

    data class Content(
        val networkViewState: NetworkViewState,
        val vpnViewState: VPNViewState,
    ) : HomeViewState
}