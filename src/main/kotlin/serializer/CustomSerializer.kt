package org.example.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.OffsetDateTime
import java.util.UUID

class CustomSerializer {
    object UUIDSerializer : KSerializer<UUID> {
        override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): UUID {
            return UUID.fromString(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: UUID) {
            encoder.encodeString(value.toString())
        }
    }

    object DatetimeSerializer : KSerializer<OffsetDateTime> {
        override val descriptor = PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): OffsetDateTime {
            return OffsetDateTime.parse(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: OffsetDateTime) {
            encoder.encodeString(value.toString())
        }
    }
}
