package tech.hookin.challengor.data.legacy

import kotlinx.serialization.Serializable

/** Links a [Challenge] to a [ChallengeType]. */
@Serializable
data class ChallengeTypeRef(
    val challengeId: Int,
    val typeName: String,
)
