package tech.hookin.challengor.domain

import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.model.ScheduleMode
import tech.hookin.challengor.model.TaskTemplate
import tech.hookin.challengor.model.Timing
import tech.hookin.challengor.model.Visibility

/**
 * One task as typed into the form. A task with [measured] = true has a daily [target] amount
 * in a [unit] (e.g. 1000 words); fields are text so they can be empty while typing.
 * [id] is set when editing an existing task, so participants' entries for it are kept.
 */
data class TaskDraft(
    val title: String = "",
    val measured: Boolean = false,
    val target: String = "",
    val unit: String = "",
    val id: String? = null,
) {
    val targetValue: Int? get() = if (measured) target.trim().toIntOrNull() else null
    val unitValue: String? get() = if (measured) unit.trim() else null
}

/** Everything a user enters to create a challenge, before it gets ids and timestamps. */
data class ChallengeDraft(
    val title: String = "",
    val description: String = "",
    val topicsInput: String = "",
    val scheduleMode: ScheduleMode = ScheduleMode.DAILY_REPEAT,
    val timing: Timing = Timing.Relative(30),
    val visibility: Visibility = Visibility.PUBLIC,
    /** Tasks per plan day; key `null` holds the daily repeating tasks. */
    val tasks: Map<Int?, List<TaskDraft>> = emptyMap(),
) {
    val topics: List<String> get() = parseTopics(topicsInput)

    /** Tasks with a title that apply to the chosen [scheduleMode] and plan length, titles trimmed. */
    val cleanTasks: Map<Int?, List<TaskDraft>>
        get() = tasks
            .filterKeys { day ->
                when (scheduleMode) {
                    ScheduleMode.DAILY_REPEAT -> day == null
                    ScheduleMode.DAY_PLAN -> day != null && day in 0 until timing.planDays
                }
            }
            .mapValues { (_, drafts) -> drafts.map { it.copy(title = it.title.trim()) }.filter { it.title.isNotEmpty() } }
            .filterValues { it.isNotEmpty() }

    /**
     * Total amount per unit over the whole plan, e.g. "words" to 100,000 for 1,000 words × 100 days.
     * Empty while the timing is invalid.
     */
    val goals: Map<String, Long>
        get() {
            if (timing.validate() != null) return emptyMap()
            val repeats = if (scheduleMode == ScheduleMode.DAILY_REPEAT) timing.planDays else 1
            return cleanTasks.values.flatten()
                .filter { it.targetValue != null && !it.unitValue.isNullOrEmpty() }
                .groupBy { it.unitValue!! }
                .mapValues { (_, drafts) -> drafts.sumOf { it.targetValue!!.toLong() } * repeats }
        }

    companion object {
        /** The form contents of an existing challenge, for editing it. */
        fun from(challenge: Challenge, tasks: List<TaskTemplate>) = ChallengeDraft(
            title = challenge.title,
            description = challenge.description,
            topicsInput = challenge.topics.joinToString(", "),
            scheduleMode = challenge.scheduleMode,
            timing = challenge.timing,
            visibility = challenge.visibility,
            tasks = tasks
                .filter { !it.deleted && it.challengeId == challenge.id }
                .sortedBy { it.position }
                .groupBy { it.dayIndex }
                .mapValues { (_, list) ->
                    list.map { task ->
                        TaskDraft(
                            title = task.title,
                            measured = task.target != null,
                            target = task.target?.toString().orEmpty(),
                            unit = task.unit.orEmpty(),
                            id = task.id,
                        )
                    }
                },
        )
    }

    /** Returns the first problem as a user-facing message, or `null` if the draft can be saved. */
    fun validate(): String? {
        val measured = cleanTasks.values.flatten().filter { it.measured }
        return when {
            title.isBlank() -> "Please enter a title"
            topics.isEmpty() -> "Add at least one topic"
            else -> timing.validate()
                ?: when {
                    cleanTasks.isEmpty() -> "Add at least one task"
                    measured.any { (it.targetValue ?: 0) <= 0 } -> "The daily target must be a positive number"
                    measured.any { it.unitValue.isNullOrEmpty() } -> "Please enter a unit, e.g. words or km"
                    else -> null
                }
        }
    }
}
