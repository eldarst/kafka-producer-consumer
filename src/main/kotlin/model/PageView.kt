package org.example.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.Nullable
import java.time.OffsetDateTime
import java.util.UUID

@Serializable
data class PageView(
    val userId: Int,
    val requestId: @Contextual UUID,
    val userName: String,
    val page: String,
    val browser: String,
    @Nullable
    val viewDate: @Contextual OffsetDateTime
)