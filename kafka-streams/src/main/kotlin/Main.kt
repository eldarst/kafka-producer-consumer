package org.example

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.example.kafka.JobPostingProducer
import org.example.kafka.JobPostingStream

fun main(): Unit = runBlocking {
    launch {
        val producer = JobPostingProducer()
        producer.startProducing()
    }

    launch {
        val consumer = JobPostingStream()
        consumer.startConsuming()
    }
}