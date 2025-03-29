package org.example.kafka

import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.KafkaJsonDeserializer
import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig
import io.confluent.kafka.serializers.KafkaJsonSerializer
import io.confluent.kafka.serializers.KafkaJsonSerializerConfig
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Printed
import org.example.config.KafkaTableConfig
import org.example.model.UserBlocked
import java.util.Properties
import kotlin.reflect.KClass

class MessagesFilter {
    private val kafkaConfig: KafkaTableConfig =
        ConfigBeanFactory.create(ConfigFactory.load().getConfig("kafkaConfig"), KafkaTableConfig::class.java)

    suspend fun startStreaming() {
        val props = getProps()

        val topology = createTopology()
        val streams = KafkaStreams(topology, props)
        Runtime.getRuntime().addShutdownHook(Thread { streams.close() })
        streams.start()
    }

    private fun getProps(): Properties {
        val props = Properties()
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.bootstrapServers
        props[StreamsConfig.REPLICATION_FACTOR_CONFIG] = kafkaConfig.replicationFactor.toString()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "streams-demo"
        return props
    }

    private fun createTopology(): Topology = StreamsBuilder().apply {
        val blockedUserTable = this.table(kafkaConfig.userBlockedTopic, Materialized.with(Serdes.Integer(), createValueSerde(UserBlocked::class)))
        blockedUserTable.toStream().print(Printed.toSysOut())
    }.build()

    private fun <T : Any> createValueSerde(valueType: KClass<T>): Serde<T> {
        val serializer = KafkaJsonSerializer<T>()
        serializer.configure(mapOf(KafkaJsonSerializerConfig.JSON_INDENT_OUTPUT to false), false)

        val deserializer = KafkaJsonDeserializer<T>()
        deserializer.configure(
            mapOf(KafkaJsonDeserializerConfig.JSON_VALUE_TYPE to valueType.qualifiedName), false
        )

        return Serdes.serdeFrom(serializer, deserializer)
    }
}