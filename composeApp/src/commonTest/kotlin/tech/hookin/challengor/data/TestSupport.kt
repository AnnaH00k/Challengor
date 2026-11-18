package tech.hookin.challengor.data

import com.russhwolf.settings.MapSettings
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** A clock that tests can move forward. */
class FakeClock(var date: LocalDate = LocalDate(2026, 1, 1)) : AppClock {
    private var tick = 0L
    override fun nowMillis(): Long = date.toEpochDays() * 86_400_000L + tick++
    override fun today(): LocalDate = date
    fun advanceDays(days: Int) {
        date = date.plus(days, DateTimeUnit.DAY)
    }
}

fun testContainer(
    settings: MapSettings = MapSettings(),
    clock: FakeClock = FakeClock(),
) = AppContainer(settings, clock)
