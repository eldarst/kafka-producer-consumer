package org.example.model

import com.github.javafaker.Faker

class JobPostingGenerator {
    fun generateRecord(): JobPostingCreated {
        val faker = Faker()
        val userId = faker.number().numberBetween(0, 10)

        return JobPostingCreated(
            userId = userId,
            jobTitle = faker.job().title(),
            jobDescription = generateJobDescription(faker),
            salary = faker.number().numberBetween(0, 300000),
        )
    }

    private fun generateJobDescription(faker: Faker): String = "${faker.job().seniority()} ${faker.job().position()}"
}