package de.marionoll.wgautoconnect.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.marionoll.wgautoconnect.home.features.network.NetworkViewModel
import de.marionoll.wgautoconnect.home.features.vpn.VPNViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject constructor(
    private val vpnViewModel: VPNViewModel,
    private val networkViewModel: NetworkViewModel,
    private val intentNavigator: IntentNavigator,
) : ViewModel() {

    fun viewState(): Flow<HomeViewState.Content> {
        return combine(
            networkViewModel.viewState,
            vpnViewModel.viewState,
        ) { networkViewState, vpnViewState ->
            HomeViewState.Content(
                networkViewState = networkViewState,
                vpnViewState = vpnViewState,
            )
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.VPN -> {
                vpnViewModel.onEvent(event)
            }

            is Event.Network -> {
                networkViewModel.onEvent(event)
            }

            Event.WireGuardClick -> {
                intentNavigator.toWireGuardPlay()
            }
        }
    }
}
