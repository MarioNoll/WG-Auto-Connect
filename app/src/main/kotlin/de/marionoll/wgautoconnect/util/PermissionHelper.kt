package de.marionoll.wgautoconnect.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionHelper
@Inject constructor(
    @ApplicationContext
    private val context: Context,
) {

    fun hasPermissions(permissions: List<String>): Boolean {
        return permissions
            .all { permission -> permission.isGranted }
    }

    private val String.isGranted: Boolean
        get() = ContextCompat.checkSelfPermission(
            context,
            this
        ) == PackageManager.PERMISSION_GRANTED
}
