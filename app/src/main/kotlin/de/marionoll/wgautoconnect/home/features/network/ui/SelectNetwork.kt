package de.marionoll.wgautoconnect.home.features.network.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.marionoll.wgautoconnect.home.Event
import de.marionoll.wgautoconnect.home.features.network.NetworkState

@Composable
fun SelectNetwork(
    modifier: Modifier = Modifier,
    viewState: NetworkState.WiFi,
    onEvent: (Event.Network) -> Unit,
) {
    AnimatedContent(
        modifier = modifier,
        targetState = viewState,
        label = "select_network_animation"
    ) { targetViewState ->
        WiFi(
            viewState = targetViewState,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun WiFi(
    viewState: NetworkState.WiFi,
    onEvent: (Event.Network) -> Unit,
) {
    when (viewState) {
        is NetworkState.WiFi.Content -> {
            LazyColumn {
                itemsIndexed(viewState.items) { index, network ->
                    NetworkItem(
                        network = network,
                        onClick = { ssid ->
                            onEvent(Event.Network.Sheet.Selected(ssid = ssid))
                        }
                    )

                    if (index != viewState.items.indices.last) {
                        HorizontalDivider()
                    }
                }
            }
        }

        NetworkState.WiFi.Empty -> {
            NetworkScanEmpty()
        }

        NetworkState.WiFi.Loading -> {
            NetworkScanSpinner()
        }
    }
}

