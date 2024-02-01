package de.marionoll.wgautoconnect.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.marionoll.wgautoconnect.home.ui.AutoConnect
import de.marionoll.wgautoconnect.home.ui.SelectNetwork
import de.marionoll.wgautoconnect.home.ui.guide.Guide

@Composable
fun HomeScreenContent(
    modifier: Modifier,
    viewState: HomeViewState.Content,
    onEvent: (Event) -> Unit,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .verticalScroll(state = rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(modifier = Modifier.size(80.dp))

        Guide(onEvent = onEvent)

        Spacer(modifier = Modifier.size(32.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.size(32.dp))

        SelectNetwork(
            networkViewState = viewState.networkViewState,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.size(32.dp))

        AutoConnect(
            viewState = viewState,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.size(24.dp))
    }
}
