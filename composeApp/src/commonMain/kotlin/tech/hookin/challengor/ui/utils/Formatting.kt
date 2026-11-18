package tech.hookin.challengor.ui.utils

import kotlinx.datetime.LocalDate
import tech.hookin.challengor.domain.ProgressStatus
import tech.hookin.challengor.model.ScheduleMode
import tech.hookin.challengor.model.Timing
import tech.hookin.challengor.model.Visibility

private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
private val WEEKDAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private const val MILLIS_PER_DAY = 86_400_000L

/** "Jan 5" */
fun LocalDate.short(): String = "${MONTHS[month.ordinal]} $day"

/** "Jan 5, 2026" */
fun LocalDate.long(): String = "${short()}, $year"

/** "Mon" */
fun LocalDate.weekdayShort(): String = WEEKDAYS[dayOfWeek.ordinal]

fun LocalDate.Companion.fromEpochMillis(millis: Long): LocalDate =
    fromEpochDays((millis / MILLIS_PER_DAY).toInt())

fun LocalDate.toEpochMillis(): Long = toEpochDays() * MILLIS_PER_DAY

fun formatDate(epochMillis: Long): String = LocalDate.fromEpochMillis(epochMillis).long()

fun Timing.describe(): String = when (this) {
    is Timing.Relative -> "$durationDays ${if (durationDays == 1) "day" else "days"} · starts when you join"
    is Timing.Fixed -> "${startDate.long()} – ${endDate.long()}"
}

val ScheduleMode.label: String
    get() = when (this) {
        ScheduleMode.DAILY_REPEAT -> "Same tasks every day"
        ScheduleMode.DAY_PLAN -> "Own to-do list per day"
    }

val Visibility.label: String
    get() = when (this) {
        Visibility.PUBLIC -> "Public"
        Visibility.LINK_ONLY -> "Only with link"
    }

val ProgressStatus.label: String
    get() = when (this) {
        ProgressStatus.UPCOMING -> "Upcoming"
        ProgressStatus.ACTIVE -> "In progress"
        ProgressStatus.COMPLETED_SUCCESS -> "Completed 100 %"
        ProgressStatus.COMPLETED_PARTIAL -> "Finished"
        ProgressStatus.ABANDONED -> "Left"
    }

fun plural(count: Int, singular: String, pluralForm: String = singular + "s"): String =
    "$count ${if (count == 1) singular else pluralForm}"

/** "100,000" */
fun formatAmount(value: Long): String {
    val digits = kotlin.math.abs(value).toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return if (value < 0) "-$grouped" else grouped
}

fun formatAmount(value: Int): String = formatAmount(value.toLong())

/** Compact form for small spaces: "950", "1.2k", "15k", "1.5M". */
fun formatAmountShort(value: Int): String = when {
    value < 1_000 -> value.toString()
    value < 10_000 -> oneDecimal(value / 100) + "k"
    value < 1_000_000 -> "${value / 1_000}k"
    else -> oneDecimal(value / 100_000) + "M"
}

/** 12 -> "1.2", 10 -> "1" */
private fun oneDecimal(tenths: Int): String =
    if (tenths % 10 == 0) "${tenths / 10}" else "${tenths / 10}.${tenths % 10}"
