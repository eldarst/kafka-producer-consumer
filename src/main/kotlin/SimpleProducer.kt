package org.example

import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.UUIDSerializer
import org.example.config.KafkaConfig
import org.example.model.PageGenerator
import org.example.model.PageView
import org.example.serializer.PageViewSerializer
import java.util.UUID
import java.util.Properties


class SimpleProducer {
    private val kafkaConfig: KafkaConfig =
        ConfigBeanFactory.create(ConfigFactory.load().getConfig("kafkaConfig"), KafkaConfig::class.java)

    suspend fun startProducing() {
        val pageGenerator = PageGenerator()
        val kafkaProducer = getProducer()

        while (true) {
            val pageViewEvent = pageGenerator.generateRecord()

            generateEvent(kafkaProducer, pageViewEvent)
        }
    }

    private suspend fun generateEvent(producer: KafkaProducer<UUID, PageView>, pageView: PageView) {
        val metadata = withContext(Dispatchers.IO) {
            producer.send(ProducerRecord(kafkaConfig.topic, pageView.id, pageView)).get()
        }
        println("Generated event with key: ${pageView.id}, partition: ${metadata.partition()}, offset: ${metadata.offset()}")
    }

    private fun getProducer(): KafkaProducer<UUID, PageView> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = UUIDSerializer::class.qualifiedName
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = PageViewSerializer::class.qualifiedName

        return KafkaProducer(props)
    }
}