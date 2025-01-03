package org.example.kafka

import org.example.config.KafkaConfig

interface ProducerKafka {
    val kafkaConfig: KafkaConfig
    suspend fun startProducing()
}