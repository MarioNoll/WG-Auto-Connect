package de.marionoll.wgautoconnect.home.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class HomeButtonStyle(val icon: ImageVector, val size: Dp) {
    Select(icon = Icons.Default.Wifi, size = 32.dp),
    AutoConnect(icon = Icons.Default.Key, size = 24.dp),
}

@Composable
fun HomeButton(
    style: HomeButtonStyle,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(
            modifier = Modifier.size(style.size),
            imageVector = style.icon,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            text = text,
            maxLines = 1,
        )

        Spacer(modifier = Modifier.size(16.dp))
    }
}

