package org.example

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.example.kafka.impl.JobPostingProducer
import org.example.kafka.impl.JobPostingStream
import org.example.kafka.impl.MongoDbStreamWriter

fun main(): Unit = runBlocking {
    launch {
        val producer = JobPostingProducer()
        producer.startProducing()
    }

    launch {
        val jobPostingStream = JobPostingStream()
        jobPostingStream.startStreaming()
    }

    launch {
        val mongoWriterStream = MongoDbStreamWriter()
        mongoWriterStream.startStreaming()
    }
}