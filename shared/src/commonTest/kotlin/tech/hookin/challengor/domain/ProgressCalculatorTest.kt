package tech.hookin.challengor.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.model.ScheduleMode
import tech.hookin.challengor.model.TaskCompletion
import tech.hookin.challengor.model.TaskTemplate
import tech.hookin.challengor.model.Timing
import tech.hookin.challengor.model.Visibility
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProgressCalculatorTest {
    private val jan1 = LocalDate(2026, 1, 1)
    private fun day(n: Int) = jan1.plus(n, DateTimeUnit.DAY)

    private fun challenge(mode: ScheduleMode, timing: Timing) = Challenge(
        id = "c1", creatorId = "u1", title = "t", description = "", topics = emptyList(),
        visibility = Visibility.PUBLIC, scheduleMode = mode, timing = timing, createdAt = 0, updatedAt = 0,
    )

    private fun done(taskId: String, date: LocalDate, done: Boolean = true, participationId: String = "p1") =
        TaskCompletion(participationId, taskId, date, done, updatedAt = 0)

    private val dailyTasks = listOf(task("a", null), task("b", null))
    private val daily = challenge(ScheduleMode.DAILY_REPEAT, Timing.Relative(3))
    private val dailyParticipation = participation(jan1, day(2))

    private fun calc(
        completions: List<TaskCompletion>,
        today: LocalDate,
        challenge: Challenge = daily,
        templates: List<TaskTemplate> = dailyTasks,
        left: Boolean = false,
    ) = ProgressCalculator.calculate(
        challenge, templates, dailyParticipation.copy(left = left), completions, today,
    )

    @Test
    fun dailyRepeatCountsEveryTaskEveryDay() {
        val progress = calc(listOf(done("a", jan1), done("b", jan1), done("a", day(1))), today = day(1))
        assertEquals(6, progress.total)
        assertEquals(3, progress.done)
        assertEquals(50, progress.percent)
        assertEquals(4, progress.dueTotal)
        assertEquals(75, progress.onTrackPercent)
        assertEquals(ProgressStatus.ACTIVE, progress.status)
        assertEquals(listOf(true, false, false), progress.days.map { it.isComplete })
    }

    @Test
    fun allDoneIsSuccess() {
        val all = (0..2).flatMap { listOf(done("a", day(it)), done("b", day(it))) }
        val progress = calc(all, today = day(2))
        assertEquals(100, progress.percent)
        assertTrue(progress.isSuccess)
    }

    @Test
    fun windowOverWithMissedTasksIsPartial() {
        val progress = calc(listOf(done("a", jan1)), today = day(5))
        assertEquals(ProgressStatus.COMPLETED_PARTIAL, progress.status)
        assertEquals(16, progress.percent) // 1 of 6, rounded down
    }

    @Test
    fun upcomingAndAbandoned() {
        assertEquals(ProgressStatus.UPCOMING, calc(emptyList(), today = jan1.plus(-1, DateTimeUnit.DAY)).status)
        assertEquals(100, calc(emptyList(), today = jan1.plus(-1, DateTimeUnit.DAY)).onTrackPercent)
        assertEquals(ProgressStatus.ABANDONED, calc(emptyList(), today = day(1), left = true).status)
    }

    @Test
    fun untickedCompletionsAndForeignDataAreIgnored() {
        val progress = calc(
            listOf(
                done("a", jan1, done = false),          // unticked
                done("b", jan1, participationId = "p2"), // someone else
                done("a", day(10)),                      // outside the window
                done("zzz", jan1),                       // unknown task
                done("b", day(1)), done("b", day(1)),     // duplicate
            ),
            today = day(2),
        )
        assertEquals(1, progress.done)
    }

    @Test
    fun dayPlanUsesTheTasksOfEachDayAndRestDays() {
        val plan = challenge(ScheduleMode.DAY_PLAN, Timing.Relative(3))
        val templates = listOf(task("d0a", 0), task("d0b", 0), task("d2", 2), task("everyday", null))
        val progress = calc(listOf(done("d0a", jan1), done("d2", day(2))), today = day(2), challenge = plan, templates = templates)
        assertEquals(listOf(2, 0, 1), progress.days.map { it.total })
        assertTrue(progress.days[1].isRestDay)
        assertEquals(3, progress.total)
        assertEquals(66, progress.percent)
    }

    @Test
    fun lateJoinerOfFixedDayPlanFollowsTheCalendar() {
        // Plan runs Jan 1–3; user joins on Jan 2, so plan day 0 is not part of their window.
        val plan = challenge(ScheduleMode.DAY_PLAN, Timing.Fixed(jan1, day(2)))
        val templates = listOf(task("d0", 0), task("d1", 1), task("d2", 2))
        val late = participation(day(1), day(2))
        val progress = ProgressCalculator.calculate(plan, templates, late, listOf(done("d1", day(1))), today = day(1))
        assertEquals(listOf(1, 2), progress.days.map { it.dayIndex })
        assertEquals(2, progress.total)
        assertEquals(50, progress.percent)
    }

    @Test
    fun challengeWithoutTasksIsNeverASuccess() {
        val progress = calc(emptyList(), today = day(5), templates = emptyList())
        assertEquals(0, progress.percent)
        assertEquals(ProgressStatus.COMPLETED_PARTIAL, progress.status)
    }
}
