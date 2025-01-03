package org.example.serializer

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Serializer
import org.example.model.JobPostingCreated

class JobPostingSerializer: Serializer<JobPostingCreated> {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun serialize(topic: String, pageView: JobPostingCreated): ByteArray {
        return json.encodeToString<JobPostingCreated>(pageView).toByteArray(Charsets.UTF_8)
    }
}