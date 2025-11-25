package tech.hookin.learningkmp.objects

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeType(
    val id: String,
    val title: String,
)
