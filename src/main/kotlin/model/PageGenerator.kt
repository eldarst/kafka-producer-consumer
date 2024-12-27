package org.example.model

import com.github.javafaker.Faker
import java.time.OffsetDateTime
import java.util.UUID

class PageGenerator {
    fun generateRecord(): PageView {
        val faker = Faker()

        return PageView(
            id = UUID.randomUUID(),
            userName = randomName(),
            browser = faker.internet().userAgentAny(),
            page = randomPage(),
            viewDate = OffsetDateTime.now()
        )
    }

    private fun randomName(): String = listOf("robbin", "joe", "daisy", "lisa").random()
    private fun randomPage(): String = listOf("/main", "/", "daisy", "lisa").random()
}