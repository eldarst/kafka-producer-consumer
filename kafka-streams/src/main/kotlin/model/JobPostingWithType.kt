package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class JobPostingWithType(
    val userId: Int,
    val jobTitle: String,
    val jobDescription: String,
    val salary: Int,
    val type: String,
)
