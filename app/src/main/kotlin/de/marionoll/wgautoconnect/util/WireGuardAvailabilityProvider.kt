package de.marionoll.wgautoconnect.util

import android.content.pm.PackageManager
import de.marionoll.wgautoconnect.service.WIRE_GUARD_PACKAGE
import javax.inject.Inject

class WireGuardAvailabilityProvider
@Inject constructor(
    private val packageManager: PackageManager,
) {
    operator fun invoke(): Boolean {
        return try {
            packageManager.getPackageInfo(WIRE_GUARD_PACKAGE, 0)
            true
        } catch (ex: PackageManager.NameNotFoundException) {
            false
        }
    }
}