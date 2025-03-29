package org.example.config

import kotlin.properties.Delegates

class KafkaTableConfig {
    var bootstrapServers: String by Delegates.notNull()
    var replicationFactor: Int by Delegates.notNull()
    var messageTopic: String by Delegates.notNull()
    var userBlockedTopic: String by Delegates.notNull()
}