package tech.hookin.challengor.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role { USER, ADMIN }

/** Public view of a user. [email] is only filled in for the user themselves (and for admins). */
@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val createdAt: Long,
    val role: Role = Role.USER,
    val email: String? = null,
) {
    val isAdmin: Boolean get() = role == Role.ADMIN
}
