package org.example.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Serializer

class JsonSerializer<T>(private val serializer: KSerializer<T>) : Serializer<T> {
    private val json = Json {
        prettyPrint = true
    }

    override fun serialize(topic: String?, data: T): ByteArray {
        return json.encodeToString(serializer, data).toByteArray(Charsets.UTF_8)
    }
}