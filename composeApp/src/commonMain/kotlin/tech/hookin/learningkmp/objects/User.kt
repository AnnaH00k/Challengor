package tech.hookin.learningkmp.objects

import kotlin.time.ExperimentalTime
import kotlin.time.Instant


data class User (
    val id: Int,
    val name: String,
    val password: String,
    val email: String,
    val registeredOn: String,
    var createdChallenges: List<Challenge> = emptyList(),
)
