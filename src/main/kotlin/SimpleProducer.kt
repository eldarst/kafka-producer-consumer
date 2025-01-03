package org.example

import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.IntegerSerializer
import org.example.config.KafkaConfig
import org.example.model.PageGenerator
import org.example.model.PageView
import org.example.serializer.PageViewSerializer

class SimpleProducer {
    private val kafkaConfig: KafkaConfig =
        ConfigBeanFactory.create(ConfigFactory.load().getConfig("kafkaConfig"), KafkaConfig::class.java)

    suspend fun startProducing() {
        val pageGenerator = PageGenerator()
        val kafkaProducer = getProducer()

        try {
            while (true) {
                val pageViewEvent = pageGenerator.generateRecord()
                generateEvent(kafkaProducer, pageViewEvent)
            }
        } finally {
            kafkaProducer.flush()
            kafkaProducer.close()
        }
    }

    private suspend fun generateEvent(producer: KafkaProducer<Int, PageView>, pageView: PageView) {
        val metadata = withContext(Dispatchers.IO) {
            producer.send(ProducerRecord(kafkaConfig.topic, pageView.userId, pageView)).get()
        }
        println("Generated event with key: ${pageView.userId}, partition: ${metadata.partition()}, offset: ${metadata.offset()}")
    }

    private fun getProducer(): KafkaProducer<Int, PageView> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to IntegerSerializer::class.qualifiedName,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to PageViewSerializer::class.qualifiedName,
            ProducerConfig.RETRIES_CONFIG to 3,
        )

        return KafkaProducer(props)
    }
}