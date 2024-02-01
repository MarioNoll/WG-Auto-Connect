package de.marionoll.wgautoconnect.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NetworkMonitorStartReceiver : BroadcastReceiver() {

    @Inject
    lateinit var serviceHandler: NetworkMonitorServiceHandler

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_BOOT_COMPLETED -> onStartIndicatorReceived()

            else -> Unit
        }
    }

    private fun onStartIndicatorReceived() {
        CoroutineScope(Dispatchers.Default).launch {
            serviceHandler.start()
        }
    }
}
