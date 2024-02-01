package de.marionoll.wgautoconnect.di

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.marionoll.wgautoconnect.data.AutoConnectState
import de.marionoll.wgautoconnect.data.SSID
import de.marionoll.wgautoconnect.data.json.JsonDataStoreFactory
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun json(): Json = Json

    @Provides
    fun jsonDataStoreFactory(
        @ApplicationContext
        context: Context,
        json: Json,
    ): JsonDataStoreFactory {
        return JsonDataStoreFactory(context, json)
    }

    @Provides
    @Singleton
    fun trustedNetworkDataStore(factory: JsonDataStoreFactory): DataStore<SSID?> {
        return factory.create(
            defaultValue = null,
            serializer = SSID.serializer().nullable,
            fileName = "trusted_network",
        )
    }

    @Provides
    @Singleton
    fun autoConnectStateDataStore(factory: JsonDataStoreFactory): DataStore<AutoConnectState?> {
        return factory.create(
            defaultValue = null,
            serializer = AutoConnectState.serializer().nullable,
            fileName = "auto_connect_state",
        )
    }
}
