package tech.hookin.challengor.model

import kotlinx.serialization.Serializable

/** A task that belongs to a challenge. Participants tick it off per day via [TaskCompletion]. */
@Serializable
data class TaskTemplate(
    val id: String,
    val challengeId: String,
    /**
     * `null` = repeats every day ([ScheduleMode.DAILY_REPEAT]).
     * Otherwise the 0-based day of the challenge this task belongs to ([ScheduleMode.DAY_PLAN]).
     */
    val dayIndex: Int?,
    val title: String,
    /** Sort order within a day. */
    val position: Int,
    val updatedAt: Long,
    val deleted: Boolean = false,
    /**
     * Daily target of a measurable task, e.g. 1000 (words). `null` = a simple checkbox task.
     * Amounts count towards the challenge-wide goal, so a short day can be made up later.
     */
    val target: Int? = null,
    /** Unit of [target], e.g. "words" or "km". */
    val unit: String? = null,
) {
    val isMeasurable: Boolean get() = target != null
}
