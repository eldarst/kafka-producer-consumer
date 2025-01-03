package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class JobPostingCreated(
    var userId: Int,
    var jobTitle: String,
    var jobDescription: String,
    var salary: Int,
)
