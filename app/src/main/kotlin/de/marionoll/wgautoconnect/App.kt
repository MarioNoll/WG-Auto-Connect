package de.marionoll.wgautoconnect

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import de.marionoll.wgautoconnect.service.NetworkMonitorServiceHandler
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var networkMonitorServiceHandler: NetworkMonitorServiceHandler

    override fun onCreate() {
        super.onCreate()

        runBlocking {
            networkMonitorServiceHandler.stopIfRequirementsNotMet()
        }
    }
}
