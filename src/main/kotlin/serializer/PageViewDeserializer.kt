package org.example.serializer

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.apache.kafka.common.serialization.Deserializer
import org.example.model.PageView

class PageViewDeserializer: Deserializer<PageView> {
    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(CustomSerializer.UUIDSerializer)
            contextual(CustomSerializer.DatetimeSerializer)
        }
    }

    override fun deserialize(topic: String, byteArray: ByteArray): PageView {
        return json.decodeFromString(byteArray.toString(Charsets.UTF_8))
    }
}