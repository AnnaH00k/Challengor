package tech.hookin.challengor.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import tech.hookin.challengor.model.Participation
import tech.hookin.challengor.model.ScheduleMode
import tech.hookin.challengor.model.TaskTemplate
import tech.hookin.challengor.model.Timing

const val MAX_CHALLENGE_DAYS = 366

/** A range of calendar days, both ends inclusive. */
data class DateWindow(val start: LocalDate, val end: LocalDate) {
    init {
        require(end >= start) { "end ($end) is before start ($start)" }
    }

    val dayCount: Int get() = start.daysUntil(end) + 1

    operator fun contains(date: LocalDate): Boolean = date in start..end

    fun dates(): List<LocalDate> = List(dayCount) { start.plus(it, DateTimeUnit.DAY) }
}

val Participation.window: DateWindow get() = DateWindow(startDate, endDate)

/** Returns an error message, or `null` if the timing is valid. */
fun Timing.validate(): String? = when (this) {
    is Timing.Relative ->
        if (durationDays !in 1..MAX_CHALLENGE_DAYS) "A challenge must last between 1 and $MAX_CHALLENGE_DAYS days" else null

    is Timing.Fixed -> when {
        endDate < startDate -> "The end date must not be before the start date"
        DateWindow(startDate, endDate).dayCount > MAX_CHALLENGE_DAYS -> "A challenge must last between 1 and $MAX_CHALLENGE_DAYS days"
        else -> null
    }
}

/** Number of days in the plan, i.e. how many day lists a [ScheduleMode.DAY_PLAN] challenge has. */
val Timing.planDays: Int
    get() = when (this) {
        is Timing.Relative -> durationDays
        is Timing.Fixed -> DateWindow(startDate, endDate).dayCount
    }

/**
 * The window of someone joining on [joinDate], or `null` if a fixed-date challenge is already over.
 * Late joiners of a fixed-date challenge only count from the day they joined.
 */
fun Timing.windowFor(joinDate: LocalDate): DateWindow? = when (this) {
    is Timing.Relative -> DateWindow(joinDate, joinDate.plus(durationDays - 1, DateTimeUnit.DAY))
    is Timing.Fixed -> if (joinDate > endDate) null else DateWindow(maxOf(startDate, joinDate), endDate)
}

/** Day 0 of the plan. For fixed dates the plan is tied to the calendar, also for late joiners. */
fun Timing.planStart(participation: Participation): LocalDate = when (this) {
    is Timing.Relative -> participation.startDate
    is Timing.Fixed -> startDate
}

/** The tasks due on plan day [dayIndex], in display order. */
fun tasksForDay(mode: ScheduleMode, templates: List<TaskTemplate>, dayIndex: Int): List<TaskTemplate> =
    templates
        .filter { !it.deleted }
        .filter {
            when (mode) {
                ScheduleMode.DAILY_REPEAT -> it.dayIndex == null
                ScheduleMode.DAY_PLAN -> it.dayIndex == dayIndex
            }
        }
        .sortedBy { it.position }

/**
 * A participant may tick tasks of today and of earlier days (to catch up),
 * but not of future days, and nothing anymore once the window is over or they left.
 */
fun canEditDay(participation: Participation, date: LocalDate, today: LocalDate): Boolean =
    !participation.left &&
        date in participation.window &&
        date <= today &&
        today <= participation.endDate
