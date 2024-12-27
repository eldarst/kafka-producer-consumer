package org.example

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    launch {
        val producer = SimpleProducer()
        producer.startProducing()
    }

    launch {
        val consumer = SimpleConsumer()
        consumer.startConsuming()
    }
}