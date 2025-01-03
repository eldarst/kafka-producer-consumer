package org.example.kafka.impl

import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.IntegerSerializer
import org.example.config.KafkaConfig
import org.example.kafka.ProducerKafka
import org.example.model.JobPostingCreated
import org.example.model.JobPostingGenerator
import org.example.serializer.JsonSerializer

class JobPostingProducer: ProducerKafka {
    override val kafkaConfig: KafkaConfig =
        ConfigBeanFactory.create(ConfigFactory.load().getConfig("kafkaConfig"), KafkaConfig::class.java)

    override suspend fun startProducing() {
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
            producer.send(ProducerRecord(kafkaConfig.jobTopic, jobPostingCreated.userId, jobPostingCreated))
                .get()
        }
        logger.info { "Generated event with key: ${jobPostingCreated.userId}, partition: ${metadata.partition()}, offset: ${metadata.offset()}" }
    }


    private fun getProducer(): KafkaProducer<Int, JobPostingCreated> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.bootstrapServers,
            ProducerConfig.RETRIES_CONFIG to 3,
        )

        return KafkaProducer(props, IntegerSerializer(), JsonSerializer(JobPostingCreated.serializer()))
    }

    companion object {
        val logger = KotlinLogging.logger {  }
    }
}