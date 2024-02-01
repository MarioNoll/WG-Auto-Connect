package de.marionoll.wgautoconnect.home.features.network.scan

import de.marionoll.wgautoconnect.data.SSID

sealed interface NetworkScanResult {
    data class PermissionMissing(val permission: String) : NetworkScanResult
    data class Content(
        val isUpToDate: Boolean,
        val networks: List<Network>,
    ) : NetworkScanResult
}

data class Network(
    val ssid: SSID,
    val signalStrength: SignalStrength,
)

enum class SignalStrength {
    Low,
    Medium,
    Good
}