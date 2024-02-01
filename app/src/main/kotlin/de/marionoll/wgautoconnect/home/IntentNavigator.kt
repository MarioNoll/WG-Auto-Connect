package de.marionoll.wgautoconnect.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import de.marionoll.wgautoconnect.service.WIRE_GUARD_PACKAGE
import javax.inject.Inject

class IntentNavigator
@Inject constructor(
    @ApplicationContext
    private val context: Context,
) {

    fun toAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .apply { data = Uri.fromParts("package", context.packageName, null) }
            .send()
    }

    fun toWireGuardPlay() {
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$WIRE_GUARD_PACKAGE")
        ).send()
    }

    fun toLocationSettings() {
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).send()
    }

    private fun Intent.send() {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(this)
    }
}