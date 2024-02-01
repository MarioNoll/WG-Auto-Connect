package de.marionoll.wgautoconnect.data

import kotlinx.serialization.Serializable

@Serializable
data class AutoConnectState(
    val tunnel: Tunnel,
    val enabled: Boolean,
)
