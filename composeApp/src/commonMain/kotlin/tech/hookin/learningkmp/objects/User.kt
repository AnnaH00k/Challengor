package tech.hookin.learningkmp.objects

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val id: Int,
    val name: String,
    val password: String,
    val email: String,
    val registeredOn: String,
    val createdChallengeIds: List<Int> = emptyList(),
)
