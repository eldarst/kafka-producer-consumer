package org.example.model

import com.github.javafaker.Faker
import java.time.OffsetDateTime
import java.util.UUID

class PageGenerator {
    private val userNames = listOf("ali", "rafael", "robbin", "joe", "daisy", "lisa", "eldar", )
    private fun randomPage(): String = listOf("/home", "/contacts", "/about", "/purchase", "/search", "/orders").random()

    fun generateRecord(): PageView {
        val faker = Faker()
        val userId = faker.number().numberBetween(0, userNames.size)

        return PageView(
            userId = userId,
            requestId = UUID.randomUUID(),
            userName = userNames[userId],
            browser = faker.internet().userAgentAny(),
            page = randomPage(),
            viewDate = OffsetDateTime.now()
        )
    }
}