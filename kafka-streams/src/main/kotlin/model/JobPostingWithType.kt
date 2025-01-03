package org.example.model

data class JobPostingWithType(
    val userId: Int,
    private val jobTitle: String,
    private val jobDescription: String,
    private val salary: Int,
    val type: String,
)
