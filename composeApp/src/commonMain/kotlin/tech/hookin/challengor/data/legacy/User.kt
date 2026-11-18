package tech.hookin.challengor.data.legacy

import kotlinx.serialization.Serializable

/** Account format of the first app version. Only read by [tech.hookin.challengor.data.LegacyMigration]. */
@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val registeredOn: String,
    val createdChallengeIds: List<Int> = emptyList(),
    val passwordHash: String = "",
    val salt: String = "",
    /** Plain-text password written by even older versions. */
    val password: String? = null,
)
