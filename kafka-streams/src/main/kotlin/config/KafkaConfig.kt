package org.example.config

import kotlin.properties.Delegates

class KafkaConfig {
    var bootstrapServers: String by Delegates.notNull()
    var typedJobTopic: String by Delegates.notNull()
    var jobTopic: String by Delegates.notNull()
    var mongoClientUri: String by Delegates.notNull()
}