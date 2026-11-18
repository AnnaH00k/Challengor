package tech.hookin.challengor.data

import kotlinx.serialization.Serializable
import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.model.Participation
import tech.hookin.challengor.model.Role
import tech.hookin.challengor.model.TaskCompletion
import tech.hookin.challengor.model.TaskTemplate
import tech.hookin.challengor.model.UserProfile

/** An account stored on this device. Moves to the server once sync exists. */
@Serializable
data class LocalAccount(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Long,
    val role: Role,
    val passwordHash: String,
    val salt: String,
) {
    fun toProfile(includeEmail: Boolean = false) = UserProfile(
        id = id,
        name = name,
        createdAt = createdAt,
        role = role,
        email = email.takeIf { includeEmail },
    )
}

/** Everything the app stores locally, saved as one JSON document. */
@Serializable
data class LocalState(
    val accounts: List<LocalAccount> = emptyList(),
    val challenges: List<Challenge> = emptyList(),
    val tasks: List<TaskTemplate> = emptyList(),
    val participations: List<Participation> = emptyList(),
    val completions: List<TaskCompletion> = emptyList(),
    val currentUserId: String? = null,
)
