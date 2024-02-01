package de.marionoll.wgautoconnect.home.features.network.scan

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import de.marionoll.wgautoconnect.data.SSID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.math.roundToInt

const val NETWORK_SCAN_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

class NetworkScanner
@Inject constructor(
    private val wifiManager: WifiManager,
    @ApplicationContext
    private val context: Context,
) {

    fun scan(): Flow<NetworkScanResult> = callbackFlow {
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        NETWORK_SCAN_PERMISSION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    trySendBlocking(
                        NetworkScanResult.PermissionMissing(permission = NETWORK_SCAN_PERMISSION)
                    )
                } else {
                    wifiManager.scanResults
                        .toResult(isUpToDate = intent.success)
                        .also(::trySendBlocking)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                NETWORK_SCAN_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            trySendBlocking(
                NetworkScanResult.PermissionMissing(permission = NETWORK_SCAN_PERMISSION)
            )
        } else {
            // An alternative api does not exist.
            @Suppress("DEPRECATION")
            val result = wifiManager.startScan()
            if (!result) {
                wifiManager.scanResults
                    .toResult(isUpToDate = false)
                    .also(::trySendBlocking)
            }
        }

        context.registerReceiver(
            wifiScanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
        )

        awaitClose {
            context.unregisterReceiver(wifiScanReceiver)
        }
    }

    private fun ScanResult.toNetwork(): Network? {
        val ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            wifiSsid?.toString() ?: return null
        } else {
            @Suppress("DEPRECATION")
            SSID
        }

        val signalMax = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wifiManager.maxSignalLevel
        } else {
            5
        }

        val signalStrengthRaw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wifiManager.calculateSignalLevel(level)
        } else {
            @Suppress("DEPRECATION")
            WifiManager.calculateSignalLevel(level, signalMax)
        }

        val signalStrength = SignalStrength.entries[
            ((signalStrengthRaw / signalMax.toFloat()) * SignalStrength.entries.size)
                .roundToInt()
                .coerceIn(0, SignalStrength.entries.indices.last)
        ]

        return Network(
            ssid = SSID(ssid.removeSurrounding("\"")),
            signalStrength = signalStrength,
        )
    }

    private fun List<ScanResult>.toResult(isUpToDate: Boolean): NetworkScanResult.Content {
        return mapNotNull { scanResult -> scanResult.toNetwork() }
            .groupBy { network -> network.ssid }
            .map { (_, networks) -> networks.maxByOrNull { network -> network.signalStrength }!! }
            .sortedByDescending { network -> network.signalStrength }
            .toScanResult(isUpToDate = isUpToDate)
    }
}

private fun List<Network>.toScanResult(isUpToDate: Boolean) = NetworkScanResult.Content(
    isUpToDate = isUpToDate,
    networks = this,
)

private val Intent.success: Boolean
    get() = getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
