package org.example

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    launch {
        val producer = SimpleProducer()
        producer.startProducing()
    }

    for (i in 1..3) {
        launch {
            val consumer = SimpleConsumer(i)
            consumer.startConsuming()
        }
    }
}