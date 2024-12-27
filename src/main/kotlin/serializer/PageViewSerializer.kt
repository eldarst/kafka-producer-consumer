package org.example.serializer

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.apache.kafka.common.serialization.Serializer
import org.example.model.PageView

class PageViewSerializer: Serializer<PageView> {
    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(CustomSerializer.UUIDSerializer)
            contextual(CustomSerializer.DatetimeSerializer)
        }
    }

    override fun serialize(topic: String, pageView: PageView): ByteArray {
        return json.encodeToString<PageView>(pageView).toByteArray(Charsets.UTF_8)
    }
}