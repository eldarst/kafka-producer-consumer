package org.example.kafka

import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.IntegerSerializer
import org.example.config.KafkaConfig
import org.example.model.JobPostingCreated
import org.example.model.JobPostingGenerator
import org.example.serializer.JobPostingSerializer

class JobPostingProducer {
    private val kafkaConfig: KafkaConfig =
        ConfigBeanFactory.create(ConfigFactory.load().getConfig("kafkaConfig"), KafkaConfig::class.java)

    suspend fun startProducing() {
        val jobPostingGenerator = JobPostingGenerator()
        val kafkaProducer = getProducer()

        try {
            while (true) {
                val pageViewEvent = jobPostingGenerator.generateRecord()
                generateEvent(kafkaProducer, pageViewEvent)
            }
        } finally {
            kafkaProducer.flush()
            kafkaProducer.close()
        }
    }

    private suspend fun generateEvent(producer: KafkaProducer<Int, JobPostingCreated>, jobPostingCreated: JobPostingCreated) {
        val metadata = withContext(Dispatchers.IO) {
            producer.send(ProducerRecord(kafkaConfig.topic, jobPostingCreated.userId, jobPostingCreated))
                .get()
        }
        println("Generated event with key: ${jobPostingCreated.userId}, partition: ${metadata.partition()}, offset: ${metadata.offset()}")
    }


    private fun getProducer(): KafkaProducer<Int, JobPostingCreated> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to IntegerSerializer::class.qualifiedName,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JobPostingSerializer::class.qualifiedName,
            ProducerConfig.RETRIES_CONFIG to 3,
        )

        return KafkaProducer(props)
    }
}