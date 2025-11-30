package tech.hookin.learningkmp.objects

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeTypeRef(
    val challengeId: Int,
    val typeName: String
)