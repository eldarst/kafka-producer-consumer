package org.example

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.example.model.PageView
import org.example.serializer.PageViewDeserializer
import java.time.Duration
import java.util.Properties
import java.util.UUID


class SimpleConsumer {
    fun startConsuming() {
        val kafkaConsumer: KafkaConsumer<UUID, PageView> = getConsumer()
        kafkaConsumer.subscribe(listOf("page-views"))

        for (i in 0..100) {
            val records: ConsumerRecords<UUID, PageView> = kafkaConsumer.poll(Duration.ofMillis(200))
            for (record in records) {
                println("$record")
                logger.info { "Received an event: " + record.partition() + ", " + record.key() + ", " + record.value() }
            }
        }
    }

    private fun getConsumer(): KafkaConsumer<UUID, PageView> {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] =
            "org.apache.kafka.common.serialization.UUIDDeserializer"
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = PageViewDeserializer::class.qualifiedName
        props[ConsumerConfig.GROUP_ID_CONFIG] = "group-a"

        return KafkaConsumer<UUID, PageView>(props)
    }

    companion object {
        val logger = KotlinLogging.logger { }
    }
}