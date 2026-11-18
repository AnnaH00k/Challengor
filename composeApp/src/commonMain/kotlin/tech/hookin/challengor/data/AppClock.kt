package tech.hookin.challengor.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Current time, injectable so tests can pin "today". */
interface AppClock {
    fun nowMillis(): Long
    fun today(): LocalDate
}

@OptIn(ExperimentalTime::class)
object SystemClock : AppClock {
    override fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
    override fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
