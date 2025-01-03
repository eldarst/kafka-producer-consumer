package org.example.kafka.impl

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
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
import org.bson.Document
import org.example.config.KafkaConfig
import org.example.kafka.StreamKafka
import org.example.model.JobPostingWithType
import org.example.serializer.JsonDeserializer
import org.example.serializer.JsonSerializer


class MongoDbStreamWriter : StreamKafka {
    override val kafkaConfig: KafkaConfig =
        ConfigBeanFactory.create(ConfigFactory.load().getConfig("kafkaConfig"), KafkaConfig::class.java)

    override suspend fun startStreaming() = withContext(Dispatchers.IO) {
        val mongoClient = MongoClients.create(getMongoClientSettings())
        val collections = mongoClient.getDatabase("job-website").getCollection("job-postings")

        val topology = createTopology(collections)
        val streams = KafkaStreams(topology, getKafkaProps())

        Runtime.getRuntime().addShutdownHook(Thread {
            streams.close()
            mongoClient.close()
        })
        streams.start()
    }


    private fun createTopology(mongoCollection: MongoCollection<Document>): Topology = StreamsBuilder().apply {
        stream(
            kafkaConfig.typedJobTopic,
            Consumed.with(
                Serdes.Integer(),
                Serdes.serdeFrom(
                    JsonSerializer(JobPostingWithType.serializer()),
                    JsonDeserializer(JobPostingWithType.serializer())
                )
            )
        ).foreach { key, value ->
            logger.info { "Saving to mongo record key: $key, value: $value" }
            writeRecordToMongo(mongoCollection, value)
        }
    }.build()

    private fun getMongoClientSettings(): MongoClientSettings {
        val serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()

        return MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(kafkaConfig.mongoClientUri))
            .serverApi(serverApi)
            .build()
    }

    private fun writeRecordToMongo(collection: MongoCollection<Document>, jobPostingWithType: JobPostingWithType) {
        collection.insertOne(
            Document().apply {
                append("userId", jobPostingWithType.userId)
                append("jobTitle", jobPostingWithType.jobTitle)
                append("jobDescription", jobPostingWithType.jobDescription)
                append("salary", jobPostingWithType.salary)
                append("type", jobPostingWithType.type)

            }
        )
    }

    private fun getKafkaProps(): Properties {
        val props = Properties()
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.bootstrapServers
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "mongodb-writer"
        return props
    }

    companion object {
        val logger = KotlinLogging.logger { }
    }
}