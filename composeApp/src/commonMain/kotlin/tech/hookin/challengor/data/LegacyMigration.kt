package tech.hookin.challengor.data

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import tech.hookin.challengor.data.legacy.ChallengeTypeRef
import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.model.Role
import tech.hookin.challengor.model.ScheduleMode
import tech.hookin.challengor.model.Timing
import tech.hookin.challengor.model.Visibility
import tech.hookin.challengor.model.newId
import kotlin.time.ExperimentalTime
import tech.hookin.challengor.data.legacy.Challenge as LegacyChallenge
import tech.hookin.challengor.data.legacy.User as LegacyUser

/**
 * Converts data written by the first app version (int ids, no schedule) into [LocalState].
 * The old keys are left untouched.
 */
class LegacyMigration(
    private val passwordHasher: PasswordHasher,
    private val clock: AppClock,
    private val roleFor: (email: String) -> Role,
) {
    fun migrate(store: JsonSettingsStore): LocalState {
        val users = store.load<List<LegacyUser>>("users").orEmpty()
        val challenges = store.load<List<LegacyChallenge>>("challenges").orEmpty()
        val refs = store.load<List<ChallengeTypeRef>>("challenge_type_refs").orEmpty()
        val currentLegacyId = store.load<LegacyUser>("current_user")?.id

        val accounts = users.mapNotNull { user -> user.toAccount()?.let { user.id to it } }.toMap()
        val newChallenges = users.flatMap { user ->
            val creator = accounts[user.id] ?: return@flatMap emptyList()
            challenges
                .filter { it.id in user.createdChallengeIds }
                .map { legacy ->
                    Challenge(
                        id = newId(),
                        creatorId = creator.id,
                        title = legacy.name,
                        description = legacy.description,
                        topics = refs.filter { it.challengeId == legacy.id }.map { it.typeName }.distinct(),
                        visibility = Visibility.PUBLIC,
                        scheduleMode = ScheduleMode.DAILY_REPEAT,
                        timing = Timing.Relative(DEFAULT_DURATION_DAYS),
                        createdAt = clock.nowMillis(),
                        updatedAt = clock.nowMillis(),
                    )
                }
        }

        return LocalState(
            accounts = accounts.values.toList(),
            challenges = newChallenges,
            currentUserId = accounts[currentLegacyId]?.id,
        )
    }

    private fun LegacyUser.toAccount(): LocalAccount? {
        val (hash, salt) = when {
            passwordHash.isNotEmpty() -> passwordHash to salt
            password != null -> passwordHasher.newSalt().let { passwordHasher.hash(password, it) to it }
            else -> return null
        }
        return LocalAccount(
            id = newId(),
            name = name,
            email = email,
            createdAt = parseMillis(registeredOn) ?: clock.nowMillis(),
            role = roleFor(email),
            passwordHash = hash,
            salt = salt,
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun parseMillis(dateTime: String): Long? = runCatching {
        LocalDateTime.parse(dateTime).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }.getOrNull()

    private companion object {
        const val DEFAULT_DURATION_DAYS = 30
    }
}
