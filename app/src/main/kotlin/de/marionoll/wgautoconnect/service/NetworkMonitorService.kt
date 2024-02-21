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
import android.net.wifi.WifiManager
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

val NETWORK_SERVICE_PERMISSIONS = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add("com.wireguard.android.permission.CONTROL_TUNNELS")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
}

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
    lateinit var wifiManager: WifiManager

    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    @Inject
    lateinit var autoConnectStateDataStore: DataStore<AutoConnectState?>

    private lateinit var network: String
    private lateinit var tunnel: String

    private var monitorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                foregroundServiceNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(
                1,
                foregroundServiceNotification,
            )
        }

        if (monitorJob == null && intent != null) {
            network = intent.getStringExtra(NETWORK_MONITOR_SERVICE_NETWORK_KEY)!!
            tunnel = intent.getStringExtra(NETWORK_MONITOR_SERVICE_TUNNEL_KEY)!!

            monitorJob = scope.launch {
                isWifiAvailable()
                    .distinctUntilChanged()
                    .collectLatest { isWifiAvailable ->
                        checkWifiState(isWifiAvailable)
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

    private fun isWifiAvailable(): Flow<Boolean> = callbackFlow {
        val networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySendBlocking(true)
            }

            override fun onLost(network: Network) {
                trySendBlocking(false)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    private suspend fun checkWifiState(isWifiAvailable: Boolean) {
        updateTunnel(
            if (isWifiAvailable) tunnelStateForWifi() else TunnelState.Up
        )
    }

    private suspend fun tunnelStateForWifi(): TunnelState {
        return connectedWifiSSID()
            .first()
            ?.let { ssid -> ssid.value == network }
            ?.let { trusted -> if (trusted) TunnelState.Down else TunnelState.Up }
            ?: TunnelState.Up
    }

    private fun connectedWifiSSID(): Flow<SSID?> = callbackFlow {
        fun WifiInfo?.sendSSID() {
            this?.ssid
                ?.removeSurrounding("\"")
                ?.let(::SSID)
                .run(::trySendBlocking)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val networkCallback = object : NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    (networkCapabilities.transportInfo as? WifiInfo)?.sendSSID()
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
        } else {
            val connectionInfo = if (wifiManager.isWifiEnabled) {
                @Suppress("DEPRECATION")
                wifiManager.connectionInfo
            } else {
                null
            }

            connectionInfo.sendSSID()
            awaitClose()
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && notificationManager.getNotificationChannel(channelId) == null
            ) {
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
