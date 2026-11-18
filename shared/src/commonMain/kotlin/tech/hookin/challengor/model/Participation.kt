package tech.hookin.challengor.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** A user taking part in a challenge, within their own date window (both dates inclusive). */
@Serializable
data class Participation(
    val id: String,
    val challengeId: String,
    val userId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val joinedAt: Long,
    val updatedAt: Long,
    val left: Boolean = false,
)
