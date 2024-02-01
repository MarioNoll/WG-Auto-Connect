package de.marionoll.wgautoconnect.service

import android.content.Intent
import de.marionoll.wgautoconnect.data.Tunnel

const val WIRE_GUARD_PACKAGE = "com.wireguard.android"

enum class TunnelState {
    Up,
    Down,
}

fun TunnelState.toIntent(tunnel: Tunnel): Intent {
    return Intent().apply {
        `package` = WIRE_GUARD_PACKAGE
        action = intentAction
        putExtra("tunnel", tunnel.value)
    }
}

private val TunnelState.intentAction: String
    get() = when (this) {
        TunnelState.Up -> "$WIRE_GUARD_PACKAGE.action.SET_TUNNEL_UP"
        TunnelState.Down -> "$WIRE_GUARD_PACKAGE.action.SET_TUNNEL_DOWN"
    }
