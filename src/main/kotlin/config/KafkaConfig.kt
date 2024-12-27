package org.example.config

import kotlin.properties.Delegates

class KafkaConfig {
    var bootstrapServers: String by Delegates.notNull()
}