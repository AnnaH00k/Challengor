package tech.hookin.challengor.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.model.Participation
import tech.hookin.challengor.model.TaskCompletion
import tech.hookin.challengor.model.TaskTemplate
import kotlin.math.floor
import kotlin.math.min

enum class ProgressStatus {
    /** The window has not started yet. */
    UPCOMING,

    /** Running and not everything is done yet. */
    ACTIVE,

    /** Every checkbox task is done and every amount goal is reached. */
    COMPLETED_SUCCESS,

    /** The window is over and something was missed. */
    COMPLETED_PARTIAL,

    /** The participant left before finishing. */
    ABANDONED,
}

data class DayProgress(
    val date: LocalDate,
    val dayIndex: Int,
    /** Tasks due on this day. */
    val total: Int,
    /** Checkbox tasks done plus measurable tasks whose daily target was reached on this day. */
    val done: Int,
    /** How many of [total] are measurable tasks. */
    val measurable: Int = 0,
    /** Logged amount per unit on this day (measurable tasks only). */
    val amounts: Map<String, Int> = emptyMap(),
    /** Daily target per unit on this day (measurable tasks only). */
    val targets: Map<String, Int> = emptyMap(),
    /**
     * At the end of this day the participant was behind: a checkbox task was left open,
     * or the running total of an amount goal was below plan. Made-up amounts don't count as behind.
     */
    val isBehind: Boolean = false,
) {
    val isRestDay: Boolean get() = total == 0
    val isMeasurableOnly: Boolean get() = total > 0 && measurable == total
    val isComplete: Boolean get() = total > 0 && done == total
}

/** An amount goal, e.g. 100,000 words, summed over the whole challenge so short days can be made up. */
data class GoalProgress(
    val unit: String,
    val goal: Long,
    val achieved: Long,
    /** What should have been reached by today. */
    val dueGoal: Long,
) {
    val isReached: Boolean get() = achieved >= goal
    val fraction: Float get() = if (goal == 0L) 0f else min(achieved, goal).toFloat() / goal
    val percent: Int get() = if (goal == 0L) 0 else (min(achieved, goal) * 100 / goal).toInt()

    /** Positive: ahead of plan, negative: behind. */
    val aheadBy: Long get() = achieved - dueGoal
}

data class ChallengeProgress(
    /** Task-days over the whole window. */
    val total: Int,
    /** Task-days credited; measurable tasks are credited proportionally to their goal. */
    val credited: Float,
    /** Task-days up to and including today. */
    val dueTotal: Int,
    val dueCredited: Float,
    val status: ProgressStatus,
    val days: List<DayProgress>,
    val goals: List<GoalProgress> = emptyList(),
) {
    /** Whole task-days done. */
    val done: Int get() = floor(credited + EPSILON).toInt()

    /** Share of the whole challenge done, 0.0 to 1.0. */
    val fraction: Float get() = if (total == 0) 0f else min(credited / total, 1f)

    /** Rounded down, so 99.6 % never shows as 100 %. */
    val percent: Int get() = if (total == 0) 0 else floor(credited * 100 / total + EPSILON).toInt().coerceAtMost(100)

    /** How much of what was due so far is done. 100 when nothing was due yet. */
    val onTrackPercent: Int
        get() = if (dueTotal == 0) 100 else floor(dueCredited * 100 / dueTotal + EPSILON).toInt().coerceAtMost(100)

    val isSuccess: Boolean get() = status == ProgressStatus.COMPLETED_SUCCESS

    private companion object {
        const val EPSILON = 1e-4f
    }
}

/** Units are compared case-insensitively, so "Words" and "words" are one goal. */
fun normalizeUnit(unit: String?): String = unit?.trim()?.lowercase().orEmpty()

object ProgressCalculator {

    private data class TaskDay(val task: TaskTemplate, val date: LocalDate, val dayIndex: Int)

    fun calculate(
        challenge: Challenge,
        templates: List<TaskTemplate>,
        participation: Participation,
        completions: List<TaskCompletion>,
        today: LocalDate,
    ): ChallengeProgress {
        val ownTemplates = templates.filter { it.challengeId == challenge.id }
        val entries = completions
            .filter { it.participationId == participation.id }
            .associateBy { it.taskTemplateId to it.date }
        val planStart = challenge.timing.planStart(participation)

        val taskDays = participation.window.dates().flatMap { date ->
            val dayIndex = planStart.daysUntil(date)
            tasksForDay(challenge.scheduleMode, ownTemplates, dayIndex).map { TaskDay(it, date, dayIndex) }
        }
        fun isDone(td: TaskDay) = entries[td.task.id to td.date]?.done == true
        fun amountOf(td: TaskDay) = entries[td.task.id to td.date]?.amount?.coerceAtLeast(0) ?: 0

        val (measurable, checkbox) = taskDays.partition { it.task.isMeasurable }
        val byUnit = measurable.groupBy { normalizeUnit(it.task.unit) }

        val goals = byUnit.map { (unit, days) ->
            GoalProgress(
                unit = days.first().task.unit?.trim().orEmpty().ifEmpty { unit },
                goal = days.sumOf { it.task.target!!.toLong() },
                achieved = days.sumOf { amountOf(it).toLong() },
                dueGoal = days.filter { it.date <= today }.sumOf { it.task.target!!.toLong() },
            )
        }

        fun credited(include: (TaskDay) -> Boolean): Float {
            val checkboxCredit = checkbox.count { include(it) && isDone(it) }.toFloat()
            val amountCredit = byUnit.values.sumOf { days ->
                val counted = days.filter(include)
                val goal = counted.sumOf { it.task.target!!.toLong() }
                if (goal == 0L) {
                    0.0
                } else {
                    // Everything logged counts, also amounts from days ahead: that's the buffer.
                    val achieved = days.sumOf { amountOf(it).toLong() }
                    min(achieved.toDouble() / goal, 1.0) * counted.size
                }
            }.toFloat()
            return checkboxCredit + amountCredit
        }

        val days = participation.window.dates().map { date ->
            val todays = taskDays.filter { it.date == date }
            DayProgress(
                date = date,
                dayIndex = planStart.daysUntil(date),
                total = todays.size,
                done = todays.count { td ->
                    if (td.task.isMeasurable) amountOf(td) >= td.task.target!! else isDone(td)
                },
                measurable = todays.count { it.task.isMeasurable },
                amounts = todays.filter { it.task.isMeasurable }
                    .groupBy { normalizeUnit(it.task.unit) }
                    .mapValues { (_, list) -> list.sumOf(::amountOf) },
                targets = todays.filter { it.task.isMeasurable }
                    .groupBy { normalizeUnit(it.task.unit) }
                    .mapValues { (_, list) -> list.sumOf { it.task.target!! } },
                isBehind = todays.any { !it.task.isMeasurable && !isDone(it) } ||
                    byUnit.any { (_, unitDays) ->
                        unitDays.any { it.date == date } && unitDays.filter { it.date <= date }.let { upTo ->
                            upTo.sumOf { amountOf(it).toLong() } < upTo.sumOf { it.task.target!!.toLong() }
                        }
                    },
            )
        }

        val total = taskDays.size
        val allChecked = checkbox.all(::isDone)
        val status = when {
            total > 0 && allChecked && goals.all { it.isReached } -> ProgressStatus.COMPLETED_SUCCESS
            participation.left -> ProgressStatus.ABANDONED
            today < participation.startDate -> ProgressStatus.UPCOMING
            today > participation.endDate -> ProgressStatus.COMPLETED_PARTIAL
            else -> ProgressStatus.ACTIVE
        }

        return ChallengeProgress(
            total = total,
            credited = credited { true },
            dueTotal = taskDays.count { it.date <= today },
            dueCredited = credited { it.date <= today },
            status = status,
            days = days,
            goals = goals,
        )
    }
}
