package org.example.serializer

import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Deserializer
import org.example.model.JobPostingCreated

class JobPostingDeserializer: Deserializer<JobPostingCreated> {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun deserialize(topic: String?, jobPosting: ByteArray): JobPostingCreated {
        return json.decodeFromString<JobPostingCreated>(jobPosting.toString(Charsets.UTF_8))
    }
}