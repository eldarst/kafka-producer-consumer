package org.example.kafka

import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.example.config.KafkaConfig
import org.example.model.JobPostingWithType
import org.example.serializer.JobPostingDeserializer
import org.example.serializer.JobPostingSerializer
import java.util.Properties

class JobPostingStream {
    private val kafkaConfig: KafkaConfig =
        ConfigBeanFactory.create(ConfigFactory.load().getConfig("kafkaConfig"), KafkaConfig::class.java)

    suspend fun startConsuming() = withContext(Dispatchers.IO) {
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
            kafkaConfig.topic,
            Consumed.with(Serdes.Integer(), Serdes.serdeFrom(JobPostingSerializer(), JobPostingDeserializer()))
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
        }.filter { key, valueWithType ->
            valueWithType.type == "high-salary"
        }.foreach { key, valueWithType ->
            println("Consumed records with key: $key, value: $valueWithType")
        }
    }.build()

}