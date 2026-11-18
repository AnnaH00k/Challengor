package tech.hookin.challengor.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import tech.hookin.challengor.domain.ChallengeDraft
import tech.hookin.challengor.domain.DateWindow
import tech.hookin.challengor.domain.canEditDay
import tech.hookin.challengor.domain.windowFor
import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.model.Participation
import tech.hookin.challengor.model.TaskCompletion
import tech.hookin.challengor.model.TaskTemplate
import tech.hookin.challengor.model.Timing
import tech.hookin.challengor.model.newId

/** Creating, joining and ticking off challenges. All writes go to the [LocalStore]. */
class ChallengeRepository(
    private val store: LocalStore,
    private val clock: AppClock,
) {
    /** Creates the challenge and lets the creator join it. Returns `null` if the draft is invalid. */
    fun create(draft: ChallengeDraft, creatorId: String): Challenge? {
        if (draft.validate() != null) return null
        val now = clock.nowMillis()
        val challenge = Challenge(
            id = newId(),
            creatorId = creatorId,
            title = draft.title.trim(),
            description = draft.description.trim(),
            topics = draft.topics,
            visibility = draft.visibility,
            scheduleMode = draft.scheduleMode,
            timing = draft.timing,
            createdAt = now,
            updatedAt = now,
        )
        val tasks = draft.cleanTasks.flatMap { (dayIndex, drafts) ->
            drafts.mapIndexed { position, task ->
                TaskTemplate(
                    id = newId(),
                    challengeId = challenge.id,
                    dayIndex = dayIndex,
                    title = task.title,
                    position = position,
                    updatedAt = now,
                    target = task.targetValue,
                    unit = task.unitValue,
                )
            }
        }
        store.update { it.copy(challenges = it.challenges + challenge, tasks = it.tasks + tasks) }
        join(challenge.id, creatorId)
        return challenge
    }

    /**
     * Saves changes to a challenge. Only the creator may edit; returns `null` otherwise or if the
     * draft is invalid. Tasks that are kept (same id) keep everyone's entries; removed tasks are
     * soft-deleted. If the time window changes, running participations are moved to the new window.
     */
    fun update(challengeId: String, draft: ChallengeDraft, userId: String): Challenge? {
        if (draft.validate() != null) return null
        val old = store.current.challenge(challengeId)?.takeIf { it.creatorId == userId } ?: return null
        val now = clock.nowMillis()
        val updated = old.copy(
            title = draft.title.trim(),
            description = draft.description.trim(),
            topics = draft.topics,
            visibility = draft.visibility,
            scheduleMode = draft.scheduleMode,
            timing = draft.timing,
            updatedAt = now,
        )

        store.update { state ->
            val existing = state.tasksOf(challengeId).associateBy { it.id }
            val reused = mutableSetOf<String>()
            val saved = draft.cleanTasks.flatMap { (dayIndex, drafts) ->
                drafts.mapIndexed { position, task ->
                    // An id can only be reused once (e.g. after "copy previous day" in an old form).
                    val base = task.id?.takeIf { it in existing && reused.add(it) }?.let(existing::getValue)
                    (base ?: TaskTemplate(newId(), challengeId, dayIndex, task.title, position, updatedAt = now)).copy(
                        dayIndex = dayIndex,
                        title = task.title,
                        position = position,
                        target = task.targetValue,
                        unit = task.unitValue,
                        updatedAt = now,
                        deleted = false,
                    )
                }
            }
            val removed = existing.values.filter { it.id !in reused }.map { it.copy(deleted = true, updatedAt = now) }
            val changedTaskIds = (saved + removed).mapTo(HashSet()) { it.id }

            state.copy(
                challenges = state.challenges.map { if (it.id == challengeId) updated else it },
                tasks = state.tasks.filterNot { it.id in changedTaskIds } + saved + removed,
                participations = state.participations.map { p ->
                    if (p.challengeId == challengeId && !p.left) p.rescheduled(old.timing, updated.timing, now) else p
                },
            )
        }
        return updated
    }

    /**
     * Moves a participation to a changed time window. Relative: same start, new length.
     * Fixed: the new dates, but late joiners still start on the day they joined.
     */
    private fun Participation.rescheduled(old: Timing, new: Timing, now: Long): Participation {
        if (old == new) return this
        // null = joined before a fixed start, so the participant takes the (new) start date.
        val joinDay = if (old is Timing.Fixed && startDate <= old.startDate) null else startDate
        val window = when (new) {
            is Timing.Relative -> DateWindow(startDate, startDate.plus(new.durationDays - 1, DateTimeUnit.DAY))
            is Timing.Fixed -> {
                val start = maxOf(new.startDate, joinDay ?: new.startDate)
                if (start > new.endDate) return this
                DateWindow(start, new.endDate)
            }
        }
        return copy(startDate = window.start, endDate = window.end, updatedAt = now)
    }

    /**
     * Joins from today on. Returns the existing participation if already joined,
     * or `null` if the challenge doesn't exist or is over.
     */
    fun join(challengeId: String, userId: String): Participation? {
        val state = store.current
        state.activeParticipation(challengeId, userId)?.let { return it }
        val challenge = state.challenge(challengeId) ?: return null
        val window = challenge.timing.windowFor(clock.today()) ?: return null
        val now = clock.nowMillis()
        val participation = Participation(
            id = newId(),
            challengeId = challengeId,
            userId = userId,
            startDate = window.start,
            endDate = window.end,
            joinedAt = now,
            updatedAt = now,
        )
        store.update { it.copy(participations = it.participations + participation) }
        return participation
    }

    fun leave(challengeId: String, userId: String) {
        val participation = store.current.activeParticipation(challengeId, userId) ?: return
        val left = participation.copy(left = true, updatedAt = clock.nowMillis())
        store.update { state ->
            state.copy(participations = state.participations.map { if (it.id == left.id) left else it })
        }
    }

    /** Ticks or unticks a task. Returns `false` if that day can't be edited (future, over, left). */
    fun setTaskDone(participationId: String, taskId: String, date: LocalDate, done: Boolean): Boolean =
        saveCompletion(participationId, date) {
            TaskCompletion(participationId, taskId, date, done, updatedAt = clock.nowMillis())
        }

    /**
     * Records how much of a measurable task was done on [date], replacing an earlier entry of that day.
     * Amounts count towards the challenge-wide goal, so a short day can be made up later.
     * Returns `false` if that day can't be edited.
     */
    fun logAmount(participationId: String, taskId: String, date: LocalDate, amount: Int): Boolean =
        saveCompletion(participationId, date) {
            val value = amount.coerceAtLeast(0)
            TaskCompletion(participationId, taskId, date, done = value > 0, updatedAt = clock.nowMillis(), amount = value)
        }

    private fun saveCompletion(participationId: String, date: LocalDate, build: () -> TaskCompletion): Boolean {
        val participation = store.current.participations.find { it.id == participationId } ?: return false
        if (!canEditDay(participation, date, clock.today())) return false

        val completion = build()
        store.update { state ->
            state.copy(completions = state.completions.filterNot { it.id == completion.id } + completion)
        }
        return true
    }

    /** Only the creator can delete. Deletion is soft so it can sync later. */
    fun delete(challengeId: String, userId: String): Boolean {
        val challenge = store.current.challenge(challengeId)?.takeIf { it.creatorId == userId } ?: return false
        val deleted = challenge.copy(deleted = true, updatedAt = clock.nowMillis())
        store.update { state ->
            state.copy(challenges = state.challenges.map { if (it.id == deleted.id) deleted else it })
        }
        return true
    }
}
