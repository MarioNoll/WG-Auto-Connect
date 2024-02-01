package de.marionoll.wgautoconnect.data.json

import androidx.datastore.core.Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

class JsonDataStoreSerializer<T>(
    private val serializer: KSerializer<T>,
    private val json: Json,
    private val default: T
) : Serializer<T> {

    override val defaultValue: T
        get() = default

    override suspend fun readFrom(input: InputStream): T {
        return json.decodeFromStream(serializer, input)
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        return json.encodeToStream(serializer, t, output)
    }
}