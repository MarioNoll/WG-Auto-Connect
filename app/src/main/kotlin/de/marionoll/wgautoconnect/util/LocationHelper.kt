package de.marionoll.wgautoconnect.util

import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocationHelper
@Inject constructor(
    private val locationManager: LocationManager,
    @ApplicationContext
    private val context: Context,
) {

    val isLocationEnabled: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            ) != Settings.Secure.LOCATION_MODE_OFF
        }
}
