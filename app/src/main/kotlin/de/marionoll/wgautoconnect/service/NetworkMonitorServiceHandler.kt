package de.marionoll.wgautoconnect.service

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.datastore.core.DataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import de.marionoll.wgautoconnect.data.AutoConnectState
import de.marionoll.wgautoconnect.data.SSID
import de.marionoll.wgautoconnect.util.PermissionHelper
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class NetworkMonitorServiceHandler
@Inject constructor(
    private val autoConnectStateDataStore: DataStore<AutoConnectState?>,
    private val trustedNetworkDataStore: DataStore<SSID?>,
    @ApplicationContext
    private val context: Context,
    private val locationManager: LocationManager,
    private val permissionHelper: PermissionHelper,
) {

    suspend fun start() {
        val trustedNetwork = trustedNetworkDataStore.data.first() ?: return
        val autoConnectData = autoConnectStateDataStore.data.first() ?: return
        if (!autoConnectData.enabled) return

        if (!stopIfRequirementsNotMet()) {
            monitorServiceIntent.apply {
                putExtra(NETWORK_MONITOR_SERVICE_NETWORK_KEY, trustedNetwork.value)
                putExtra(NETWORK_MONITOR_SERVICE_TUNNEL_KEY, autoConnectData.tunnel.value)
            }.also(context::startForegroundService)
        }
    }

    suspend fun stopIfRequirementsNotMet(): Boolean {
        return if (requirementsSatisfied()) {
            false
        } else {
            stop()
            true
        }
    }

    suspend fun stop() {
        autoConnectStateDataStore.updateData { state ->
            state?.copy(enabled = false)
        }
        context.stopService(monitorServiceIntent)
    }

    private fun requirementsSatisfied(): Boolean {
        return permissionHelper.hasPermissions(NETWORK_SERVICE_PERMISSIONS)
            && locationManager.isLocationEnabled
    }

    private val monitorServiceIntent: Intent
        get() = Intent(context, NetworkMonitorService::class.java)
}
