package de.marionoll.wgautoconnect.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import de.marionoll.wgautoconnect.MainActivity
import de.marionoll.wgautoconnect.R
import de.marionoll.wgautoconnect.data.AutoConnectState
import de.marionoll.wgautoconnect.data.SSID
import de.marionoll.wgautoconnect.data.Tunnel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

const val NETWORK_MONITOR_SERVICE_NETWORK_KEY = "network"
const val NETWORK_MONITOR_SERVICE_TUNNEL_KEY = "tunnel"

val NETWORK_SERVICE_PERMISSIONS = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    "com.wireguard.android.permission.CONTROL_TUNNELS",
)

@AndroidEntryPoint
class NetworkMonitorService : Service() {

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    @Inject
    lateinit var autoConnectStateDataStore: DataStore<AutoConnectState?>

    private lateinit var network: String
    private lateinit var tunnel: String

    private var monitorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            1,
            foregroundServiceNotification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )

        if (monitorJob == null && intent != null) {
            network = intent.getStringExtra(NETWORK_MONITOR_SERVICE_NETWORK_KEY)!!
            tunnel = intent.getStringExtra(NETWORK_MONITOR_SERVICE_TUNNEL_KEY)!!

            monitorJob = scope.launch {
                transportCapabilities()
                    .distinctUntilChanged()
                    .collectLatest { transportCapabilities ->
                        checkWifiState(transportCapabilities)
                    }
            }
        }

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        scope.cancel()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun transportCapabilities(): Flow<List<Int>> = callbackFlow {
        val defaultNetworkCallback = object : NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                listOf(
                    NetworkCapabilities.TRANSPORT_CELLULAR,
                    NetworkCapabilities.TRANSPORT_WIFI,
                )
                    .filter(networkCapabilities::hasTransport)
                    .let(::trySendBlocking)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(defaultNetworkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(defaultNetworkCallback)
        }
    }

    private suspend fun checkWifiState(transportCapabilities: List<Int>) {
        transportCapabilities
            .contains(NetworkCapabilities.TRANSPORT_WIFI)
            .let { wifiConnected -> if (wifiConnected) tunnelStateForWifi() else TunnelState.Up }
            .also(::updateTunnel)
    }

    private suspend fun tunnelStateForWifi(): TunnelState {
        return connectedWifiSSID()
            .first()
            ?.let { ssid -> ssid.value == network }
            ?.let { trusted -> if (trusted) TunnelState.Down else TunnelState.Up }
            ?: TunnelState.Up
    }

    private fun connectedWifiSSID(): Flow<SSID?> = callbackFlow {
        fun NetworkCapabilities.onChanged() {
            (transportInfo as? WifiInfo)
                ?.ssid
                ?.removeSurrounding("\"")
                ?.let(::SSID)
                .run(::trySendBlocking)
        }

        val networkCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            object : NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    networkCapabilities.onChanged()
                }
            }
        } else {
            object : NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    networkCapabilities.onChanged()
                }
            }
        }

        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build(),
            networkCallback,
        )

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    private fun updateTunnel(desiredState: TunnelState) {
        desiredState
            .takeIf(::shouldUpdateTunnel)
            ?.toIntent(Tunnel(tunnel))
            ?.also(::sendBroadcast)
    }

    private fun shouldUpdateTunnel(desiredState: TunnelState): Boolean {
        return when (desiredState) {
            TunnelState.Up -> !vpnEnabled
            TunnelState.Down -> vpnEnabled
        }
    }

    private val vpnEnabled: Boolean
        get() = connectivityManager
            .getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

    private val foregroundServiceNotification: Notification
        get() {
            val contentIntent = Intent(this, MainActivity::class.java)
                .let { PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE) }

            val channelId = "network_service"
            if (notificationManager.getNotificationChannel(channelId) == null) {
                notificationManager.createNotificationChannel(
                    NotificationChannel(
                        channelId,
                        context.getString(R.string.vpn_auto_connect_notification_channel),
                        NotificationManager.IMPORTANCE_LOW,
                    )
                )
            }

            return NotificationCompat.Builder(context, channelId)
                .apply {
                    setContentTitle(R.string.network_service_active)
                    setOngoing(true)
                    setSmallIcon(R.drawable.notification_app_icon)
                    setContentIntent(contentIntent)
                    foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
                }
                .build()
        }
}

context(Context)
fun NotificationCompat.Builder.setContentTitle(@StringRes title: Int) {
    setContentTitle(
        getString(title)
    )
}
