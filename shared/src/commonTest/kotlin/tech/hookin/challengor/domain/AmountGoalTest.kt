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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** "Write 1000 words a day": amounts count towards the total goal, so short days can be made up. */
class AmountGoalTest {
    private val jan1 = LocalDate(2026, 1, 1)
    private fun day(n: Int) = jan1.plus(n, DateTimeUnit.DAY)

    private fun challenge(mode: ScheduleMode = ScheduleMode.DAILY_REPEAT) = Challenge(
        id = "c1", creatorId = "u1", title = "Write", description = "", topics = emptyList(),
        visibility = Visibility.PUBLIC, scheduleMode = mode, timing = Timing.Relative(3), createdAt = 0, updatedAt = 0,
    )

    private fun measured(id: String, target: Int, unit: String = "words", dayIndex: Int? = null) =
        TaskTemplate(id, "c1", dayIndex, id, 0, updatedAt = 0, target = target, unit = unit)

    private fun log(taskId: String, n: Int, amount: Int) =
        TaskCompletion("p1", taskId, day(n), done = amount > 0, updatedAt = 0, amount = amount)

    private fun calc(
        completions: List<TaskCompletion>,
        today: LocalDate,
        templates: List<TaskTemplate> = listOf(measured("write", 1000)),
        challenge: Challenge = challenge(),
    ) = ProgressCalculator.calculate(challenge, templates, participation(jan1, day(2)), completions, today)

    @Test
    fun aShortDayMadeUpLaterStillReachesTheGoal() {
        val progress = calc(listOf(log("write", 0, 500), log("write", 1, 1500), log("write", 2, 1000)), today = day(2))
        val goal = progress.goals.single()
        assertEquals(3000, goal.goal)
        assertEquals(3000, goal.achieved)
        assertTrue(goal.isReached)
        assertEquals(100, progress.percent)
        assertTrue(progress.isSuccess, "day 1 was short, but it was made up")
        assertFalse(progress.days[0].isComplete, "the day itself stays below its target")
        assertTrue(progress.days[0].isBehind, "at the end of day 1 the writer was behind")
        assertFalse(progress.days[1].isBehind, "caught up on day 2")
    }

    @Test
    fun workingAheadIsABuffer() {
        val progress = calc(listOf(log("write", 0, 2000)), today = day(1))
        val goal = progress.goals.single()
        assertEquals(0, goal.aheadBy, "2000 written, 2000 planned by day 2")
        assertFalse(progress.days[1].isBehind, "nothing written on day 2, but covered by day 1")
        assertEquals(100, progress.onTrackPercent)
        assertEquals(66, progress.percent)
    }

    @Test
    fun behindPlanIsNegative() {
        val progress = calc(listOf(log("write", 0, 300)), today = day(0))
        assertEquals(-700, progress.goals.single().aheadBy)
        assertTrue(progress.days[0].isBehind)
        assertEquals(10, progress.percent) // 300 of 3000
        assertEquals(30, progress.onTrackPercent) // 300 of 1000 due
    }

    @Test
    fun moreThanTheGoalIsCappedAt100() {
        val progress = calc(listOf(log("write", 0, 5000)), today = day(0))
        assertEquals(100, progress.percent)
        assertTrue(progress.isSuccess)
    }

    @Test
    fun checkboxAndAmountTasksAreWeightedPerTaskDay() {
        val templates = listOf(
            measured("write", 1000),
            TaskTemplate("read", "c1", null, "Read", 1, updatedAt = 0),
        )
        val progress = calc(
            listOf(log("write", 0, 1500), TaskCompletion("p1", "read", day(0), done = true, updatedAt = 0)),
            today = day(0),
            templates = templates,
        )
        // 6 task-days: 1 reading day done + 1500/3000 words = half of 3 writing days.
        assertEquals(6, progress.total)
        assertEquals(41, progress.percent) // 2.5 / 6
        assertFalse(progress.isSuccess)
    }

    @Test
    fun dayPlanAmountsOfTheSameUnitArePooled() {
        val templates = listOf(measured("run1", 5, "km", dayIndex = 0), measured("run3", 8, "km", dayIndex = 2))
        val progress = calc(
            listOf(log("run1", 0, 3), log("run3", 2, 10)),
            today = day(2),
            templates = templates,
            challenge = challenge(ScheduleMode.DAY_PLAN),
        )
        val goal = progress.goals.single()
        assertEquals(13, goal.goal)
        assertEquals(13, goal.achieved)
        assertTrue(progress.isSuccess)
        assertEquals(listOf(true, false, false), progress.days.map { it.isBehind }, "short day 1 made up on day 3")
    }

    @Test
    fun unitsAreComparedCaseInsensitively() {
        val templates = listOf(measured("a", 500, "Words"), measured("b", 500, " words"))
        val progress = calc(listOf(log("a", 0, 1000)), today = day(0), templates = templates)
        assertEquals(1, progress.goals.size)
        assertEquals(0, progress.goals.single().aheadBy)
    }
}
