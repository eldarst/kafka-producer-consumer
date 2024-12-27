package org.example

fun main() {
    val producer = SimpleProducer()
    producer.startProducing()

    val consumer = SimpleConsumer()
    consumer.startConsuming()
}
