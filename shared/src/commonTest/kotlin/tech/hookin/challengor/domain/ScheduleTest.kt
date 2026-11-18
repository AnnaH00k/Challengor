package tech.hookin.challengor.domain

import kotlinx.datetime.LocalDate
import tech.hookin.challengor.model.Participation
import tech.hookin.challengor.model.ScheduleMode
import tech.hookin.challengor.model.TaskTemplate
import tech.hookin.challengor.model.Timing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScheduleTest {
    private val jan1 = LocalDate(2026, 1, 1)

    @Test
    fun relativeWindowStartsOnJoinDay() {
        val window = assertNotNull(Timing.Relative(30).windowFor(LocalDate(2026, 1, 15)))
        assertEquals(LocalDate(2026, 1, 15), window.start)
        assertEquals(LocalDate(2026, 2, 13), window.end)
        assertEquals(30, window.dayCount)
    }

    @Test
    fun fixedWindowForLateJoinerStartsOnJoinDay() {
        val timing = Timing.Fixed(jan1, LocalDate(2026, 1, 10))
        assertEquals(DateWindow(jan1, LocalDate(2026, 1, 10)), timing.windowFor(LocalDate(2025, 12, 20)))
        assertEquals(DateWindow(LocalDate(2026, 1, 5), LocalDate(2026, 1, 10)), timing.windowFor(LocalDate(2026, 1, 5)))
        assertNull(timing.windowFor(LocalDate(2026, 1, 11)))
    }

    @Test
    fun timingValidation() {
        assertNull(Timing.Relative(1).validate())
        assertNotNull(Timing.Relative(0).validate())
        assertNotNull(Timing.Relative(MAX_CHALLENGE_DAYS + 1).validate())
        assertNull(Timing.Fixed(jan1, jan1).validate())
        assertNotNull(Timing.Fixed(LocalDate(2026, 1, 2), jan1).validate())
        assertNotNull(Timing.Fixed(jan1, LocalDate(2028, 1, 1)).validate())
    }

    @Test
    fun planDays() {
        assertEquals(7, Timing.Relative(7).planDays)
        assertEquals(31, Timing.Fixed(jan1, LocalDate(2026, 1, 31)).planDays)
    }

    @Test
    fun tasksForDayByMode() {
        val templates = listOf(
            task("every-b", dayIndex = null, position = 1),
            task("every-a", dayIndex = null, position = 0),
            task("day0", dayIndex = 0),
            task("day1", dayIndex = 1),
            task("deleted", dayIndex = null, deleted = true),
        )
        assertEquals(listOf("every-a", "every-b"), tasksForDay(ScheduleMode.DAILY_REPEAT, templates, 5).map { it.id })
        assertEquals(listOf("day1"), tasksForDay(ScheduleMode.DAY_PLAN, templates, 1).map { it.id })
        assertEquals(emptyList(), tasksForDay(ScheduleMode.DAY_PLAN, templates, 2))
    }

    @Test
    fun canEditOnlyTodayAndPastDaysWhileRunning() {
        val p = participation(jan1, LocalDate(2026, 1, 10))
        val today = LocalDate(2026, 1, 5)
        assertTrue(canEditDay(p, today, today))
        assertTrue(canEditDay(p, jan1, today))
        assertFalse(canEditDay(p, LocalDate(2026, 1, 6), today), "future day")
        assertFalse(canEditDay(p, LocalDate(2025, 12, 31), today), "before window")
        assertFalse(canEditDay(p, jan1, LocalDate(2026, 1, 11)), "window over")
        assertFalse(canEditDay(p.copy(left = true), today, today), "left")
    }
}

internal fun task(id: String, dayIndex: Int?, position: Int = 0, deleted: Boolean = false, challengeId: String = "c1") =
    TaskTemplate(id, challengeId, dayIndex, id, position, updatedAt = 0, deleted = deleted)

internal fun participation(start: LocalDate, end: LocalDate, left: Boolean = false) =
    Participation("p1", "c1", "u1", start, end, joinedAt = 0, updatedAt = 0, left = left)
