package org.example.kafka.impl

import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.example.config.KafkaConfig
import org.example.kafka.StreamKafka
import org.example.model.JobPostingCreated
import org.example.model.JobPostingWithType
import org.example.serializer.JsonDeserializer
import org.example.serializer.JsonSerializer

class JobPostingStream: StreamKafka {
    override val kafkaConfig: KafkaConfig =
        ConfigBeanFactory.create(ConfigFactory.load().getConfig("kafkaConfig"), KafkaConfig::class.java)

    override suspend fun startStreaming() = withContext(Dispatchers.IO) {
        val props = getProps()

        val topology = createTopology()
        val streams = KafkaStreams(topology, props)
        Runtime.getRuntime().addShutdownHook(Thread { streams.close() })
        streams.start()
    }

    private fun getProps(): Properties {
        val props = Properties()
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.bootstrapServers
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "streams-demo"
        return props
    }

    private fun createTopology(): Topology = StreamsBuilder().apply {
        stream(
            kafkaConfig.jobTopic,
            Consumed.with(
                Serdes.Integer(),
                Serdes.serdeFrom(
                    JsonSerializer(JobPostingCreated.serializer()),
                    JsonDeserializer(JobPostingCreated.serializer())
                )
            )
        ).mapValues { value ->
            JobPostingWithType(
                userId = value.userId,
                jobTitle = value.jobTitle,
                jobDescription = value.jobDescription,
                salary = value.salary,
                type = when {
                    value.salary < 1_000 -> "pro-bono"
                    value.salary < 60_000 -> "normal"
                    else -> "high-salary"
                }
            )
        }.peek { key, valueWithType ->
            logger.info { "Consumed records with key: $key, value: $valueWithType" }
        }.to(
            kafkaConfig.typedJobTopic, Produced.with(
                Serdes.Integer(),
                Serdes.serdeFrom(
                    JsonSerializer(JobPostingWithType.serializer()),
                    JsonDeserializer(JobPostingWithType.serializer())
                )
            )
        )
    }.build()

    companion object {
        val logger = KotlinLogging.logger {  }
    }
}