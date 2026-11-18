package tech.hookin.challengor.data

import kotlinx.datetime.LocalDate
import tech.hookin.challengor.domain.ChallengeProgress
import tech.hookin.challengor.domain.ProgressCalculator
import tech.hookin.challengor.domain.normalizeTopic
import tech.hookin.challengor.domain.windowFor
import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.model.Participation
import tech.hookin.challengor.model.TaskTemplate
import tech.hookin.challengor.model.Visibility

// Read-only views on the local state. Pure functions, so ViewModels can map flows with them.

enum class ChallengeSort(val label: String) {
    NEWEST("Newest"),
    POPULAR("Most joined"),
    TITLE("A–Z"),
}

data class TopicCount(val topic: String, val count: Int)

fun LocalState.account(userId: String): LocalAccount? = accounts.find { it.id == userId }

fun LocalState.challenge(id: String): Challenge? = challenges.find { it.id == id && !it.deleted }

fun LocalState.tasksOf(challengeId: String): List<TaskTemplate> =
    tasks.filter { it.challengeId == challengeId && !it.deleted }

/** The user's current (not left) participation in a challenge. */
fun LocalState.activeParticipation(challengeId: String, userId: String): Participation? =
    participations
        .filter { it.challengeId == challengeId && it.userId == userId && !it.left }
        .maxByOrNull { it.joinedAt }

/** The most recent participation per challenge of a user, including ones they left. */
fun LocalState.latestParticipationsOf(userId: String): List<Participation> =
    participations
        .filter { it.userId == userId && challenge(it.challengeId) != null }
        .groupBy { it.challengeId }
        .map { (_, entries) -> entries.maxBy { it.joinedAt } }

fun LocalState.participantCount(challengeId: String): Int =
    participations.filter { it.challengeId == challengeId && !it.left }.distinctBy { it.userId }.size

fun LocalState.progressOf(participation: Participation, today: LocalDate): ChallengeProgress? {
    val challenge = challenge(participation.challengeId) ?: return null
    return ProgressCalculator.calculate(
        challenge = challenge,
        templates = tasksOf(challenge.id),
        participation = participation,
        completions = completions.filter { it.participationId == participation.id },
        today = today,
    )
}

/** Whether someone could join today (fixed-date challenges close after their end date). */
fun Challenge.isOpenToJoin(today: LocalDate): Boolean = timing.windowFor(today) != null

fun LocalState.publicChallenges(): List<Challenge> =
    challenges.filter { !it.deleted && it.visibility == Visibility.PUBLIC }

/**
 * Public challenges matching [query] (title, description or topic) and, if set, exactly [topic].
 */
fun LocalState.searchChallenges(query: String, topic: String?, sort: ChallengeSort): List<Challenge> {
    val text = query.trim()
    val matches = publicChallenges()
        .filter { topic == null || topic in it.topics }
        .filter { challenge ->
            text.isEmpty() ||
                challenge.title.contains(text, ignoreCase = true) ||
                challenge.description.contains(text, ignoreCase = true) ||
                challenge.topics.any { it.contains(normalizeTopic(text)) }
        }
    return when (sort) {
        ChallengeSort.NEWEST -> matches.sortedByDescending { it.createdAt }
        ChallengeSort.POPULAR -> matches.sortedWith(
            compareByDescending<Challenge> { participantCount(it.id) }.thenByDescending { it.createdAt },
        )
        ChallengeSort.TITLE -> matches.sortedBy { it.title.lowercase() }
    }
}

/** Topics of public challenges, most used first. */
fun LocalState.topicCounts(): List<TopicCount> =
    publicChallenges()
        .flatMap { it.topics }
        .groupingBy { it }
        .eachCount()
        .map { (topic, count) -> TopicCount(topic, count) }
        .sortedWith(compareByDescending<TopicCount> { it.count }.thenBy { it.topic })

fun LocalState.searchUsers(query: String): List<LocalAccount> {
    val text = query.trim()
    return accounts
        .filter { text.isEmpty() || it.name.contains(text, ignoreCase = true) }
        .sortedBy { it.name.lowercase() }
}

/** Challenges a user created that [viewerId] may see: all of them for the creator, else only public ones. */
fun LocalState.createdBy(userId: String, viewerId: String?): List<Challenge> =
    challenges
        .filter { it.creatorId == userId && !it.deleted }
        .filter { userId == viewerId || it.visibility == Visibility.PUBLIC }
        .sortedByDescending { it.createdAt }
