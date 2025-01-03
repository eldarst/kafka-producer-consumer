package org.example.kafka

import org.example.config.KafkaConfig

interface StreamKafka {
    val kafkaConfig: KafkaConfig
    suspend fun startStreaming()
}