package org.example

import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.UUIDDeserializer
import org.example.config.KafkaConfig
import org.example.model.PageView
import org.example.serializer.PageViewDeserializer
import java.time.Duration
import java.util.Properties
import java.util.UUID


class SimpleConsumer {
    private val kafkaConfig: KafkaConfig =
        ConfigBeanFactory.create(ConfigFactory.load().getConfig("kafkaConfig"), KafkaConfig::class.java)

    suspend fun startConsuming() {
        val kafkaConsumer: KafkaConsumer<UUID, PageView> = getConsumer()
        kafkaConsumer.subscribe(listOf(kafkaConfig.topic))
        withContext(Dispatchers.IO) {
            while (true) {
                val records: ConsumerRecords<UUID, PageView> = kafkaConsumer.poll(Duration.ofMillis(100))
                kafkaConsumer.commitSync()
                println("Polled the records")
                for (record in records) {
                    println("Received an event key: ${record.key()}, partition: ${record.partition()}, offset: ${record.offset()}, value: ${record.value()}")
                }
            }
        }
    }

    private fun getConsumer(): KafkaConsumer<UUID, PageView> {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.bootstrapServers
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = UUIDDeserializer::class.qualifiedName
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = PageViewDeserializer::class.qualifiedName
        props[ConsumerConfig.GROUP_ID_CONFIG] = "simple-consumer-group"

        return KafkaConsumer<UUID, PageView>(props)
    }
}