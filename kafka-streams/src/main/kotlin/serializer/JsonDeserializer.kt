package org.example.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Deserializer

class JsonDeserializer<T>(private val serializer: KSerializer<T>) : Deserializer<T> {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun deserialize(topic: String?, data: ByteArray): T {
        return json.decodeFromString(serializer, data.toString(Charsets.UTF_8))
    }
}