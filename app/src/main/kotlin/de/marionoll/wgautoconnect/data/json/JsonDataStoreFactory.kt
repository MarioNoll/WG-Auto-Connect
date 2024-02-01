package de.marionoll.wgautoconnect.data.json

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class JsonDataStoreFactory(
    private val context: Context,
    private val json: Json,
) {
    fun <T> create(
        defaultValue: T,
        serializer: KSerializer<T>,
        fileName: String,
    ): DataStore<T> {
        return DataStoreFactory.create(
            JsonDataStoreSerializer(
                serializer = serializer,
                default = defaultValue,
                json = json,
            ),
            corruptionHandler = ReplaceFileCorruptionHandler { defaultValue },
        ) {
            context.dataStoreFile(fileName)
        }
    }
}