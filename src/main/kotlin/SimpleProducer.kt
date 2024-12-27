package org.example

import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.example.config.KafkaConfig
import org.example.model.PageGenerator
import org.example.model.PageView
import org.example.serializer.PageViewSerializer
import java.util.Properties
import java.util.UUID


class SimpleProducer {
    private val kafkaConfig: KafkaConfig = ConfigBeanFactory.create(ConfigFactory.load().getConfig("kafkaConfig"), KafkaConfig::class.java)

    fun startProducing() {
        val pageGenerator = PageGenerator()
        val kafkaProducer = getProducer()

        for (i in 0..100) {
            logger.info { "Generating a page view event" }
            println("Generating a page view event")
            val pageViewEvent = pageGenerator.generateRecord()
            logger.info { "Page view with id ${pageViewEvent.id} was generated" }
            println("Page view with id ${pageViewEvent.id} was generated")

            logger.info { "Publishing message to topic" }
            println("Publishing message to topic")

            generateEvent(kafkaProducer, pageViewEvent)

            Thread.sleep(100)
        }
    }

    private fun generateEvent(producer: KafkaProducer<UUID, PageView>, pageView: PageView) {
        producer.send(ProducerRecord("page-views", pageView.id, pageView))
        logger.info { "Event send" }
    }

    private fun getProducer(): KafkaProducer<UUID, PageView> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.UUIDSerializer"
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = PageViewSerializer::class.qualifiedName

        return KafkaProducer(props)
    }

    companion object {
        val logger = KotlinLogging.logger {  }
    }
}