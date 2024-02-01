package de.marionoll.wgautoconnect.di

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.core.app.NotificationManagerCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object SystemServiceModule {

    @Provides
    fun wifiManager(@ApplicationContext context: Context): WifiManager {
        return context.getSystemService(WifiManager::class.java)
    }

    @Provides
    fun connectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(ConnectivityManager::class.java)
    }

    @Provides
    fun locationManager(@ApplicationContext context: Context): LocationManager {
        return context.getSystemService(LocationManager::class.java)
    }

    @Provides
    fun packageManager(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }

    @Provides
    fun notificationManager(@ApplicationContext context: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }
}
