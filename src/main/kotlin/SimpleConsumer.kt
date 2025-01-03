package org.example

import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.UUIDDeserializer
import org.example.config.KafkaConfig
import org.example.model.PageView
import org.example.serializer.PageViewDeserializer
import java.time.Duration
import java.util.Properties
import java.util.UUID


class SimpleConsumer(val consumerId: Int) {
    private val kafkaConfig: KafkaConfig =
        ConfigBeanFactory.create(ConfigFactory.load().getConfig("kafkaConfig"), KafkaConfig::class.java)

    suspend fun startConsuming() {
        val kafkaConsumer: KafkaConsumer<Int, PageView> = getConsumer()
        kafkaConsumer.subscribe(listOf(kafkaConfig.topic))
        try {
            withContext(Dispatchers.IO) {
                while (true) {
                    val records: ConsumerRecords<Int, PageView> = kafkaConsumer.poll(Duration.ofMillis(1000))
                    for (record in records) {
                        println("Consumer: '$consumerId' received an event key: '${record.key()}', partition: '${record.partition()}', offset: '${record.offset()}'")
                    }
                    kafkaConsumer.commitAsync()
                }
            }
        } catch (ex: Exception) {
            kafkaConsumer.close()
        }
    }

    private fun getConsumer(): KafkaConsumer<Int, PageView> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to IntegerDeserializer::class.qualifiedName,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to PageViewDeserializer::class.qualifiedName,
            ConsumerConfig.GROUP_ID_CONFIG to "simple-consumer-group",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )

        return KafkaConsumer<Int, PageView>(props)
    }
}