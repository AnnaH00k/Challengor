package tech.hookin.challengor.data.legacy

import kotlinx.serialization.Serializable

/** Challenge format of the first app version. Only read by [tech.hookin.challengor.data.LegacyMigration]. */
@Serializable
data class Challenge(
    val id: Int,
    val name: String,
    val description: String,
)
