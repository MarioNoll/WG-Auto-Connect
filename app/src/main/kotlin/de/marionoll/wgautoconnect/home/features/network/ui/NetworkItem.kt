package de.marionoll.wgautoconnect.home.features.network.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Wifi1Bar
import androidx.compose.material.icons.filled.Wifi2Bar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.marionoll.wgautoconnect.data.SSID
import de.marionoll.wgautoconnect.home.features.network.scan.Network
import de.marionoll.wgautoconnect.home.features.network.scan.SignalStrength

@Composable
fun NetworkItem(
    network: Network,
    onClick: (SSID) -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { onClick(network.ssid) }
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = network.signalStrength.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = network.ssid.value,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val SignalStrength.icon: ImageVector
    get() = when (this) {
        SignalStrength.Low -> Icons.Default.Wifi1Bar
        SignalStrength.Medium -> Icons.Default.Wifi2Bar
        SignalStrength.Good -> Icons.Default.Wifi
    }