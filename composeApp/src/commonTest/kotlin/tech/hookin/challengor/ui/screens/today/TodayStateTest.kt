package tech.hookin.challengor.ui.screens.today

import kotlinx.datetime.LocalDate
import tech.hookin.challengor.data.FakeClock
import tech.hookin.challengor.data.testContainer
import tech.hookin.challengor.domain.ChallengeDraft
import tech.hookin.challengor.domain.TaskDraft
import tech.hookin.challengor.model.Timing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TodayStateTest {
    private val jan1 = LocalDate(2026, 1, 1) // a Thursday
    private val clock = FakeClock(jan1)
    private val container = testContainer(clock = clock)
    private val anna = assertNotNull(container.auth.register("Anna", "anna@example.com", "secret1"))
    private val challenge = assertNotNull(
        container.challenges.create(
            ChallengeDraft(title = "Run", topicsInput = "Sport", timing = Timing.Relative(5), tasks = mapOf(null to listOf(TaskDraft("Run")))),
            anna.id,
        ),
    )

    init {
        clock.date = LocalDate(2026, 1, 3)
    }

    private fun stateOn(date: LocalDate) =
        container.store.current.todayUiState(anna.id, clock.today(), date)

    @Test
    fun pastDayIsShownAndCanBeCaughtUp() {
        val day = assertNotNull(stateOn(jan1).active.singleOrNull())
        assertEquals(jan1, day.date)
        assertTrue(day.editable)
        assertTrue(container.challenges.setTaskDone(day.participation.id, day.tasks.single().task.id, jan1, true))
        assertTrue(stateOn(jan1).active.single().allDone)
        assertFalse(stateOn(clock.today()).active.single().allDone, "only the past day was ticked")
    }

    @Test
    fun futureDayIsShownReadOnly() {
        val day = assertNotNull(stateOn(LocalDate(2026, 1, 5)).active.singleOrNull())
        assertEquals(challenge.id, day.challenge.id)
        assertFalse(day.editable)
    }

    @Test
    fun dayOutsideTheWindowIsEmpty() {
        val state = stateOn(LocalDate(2026, 1, 6))
        assertTrue(state.active.isEmpty())
        assertFalse(state.isEmpty, "still taking part in a challenge")
    }


    @Test
    fun startingSoonOnlyShowsOnToday() {
        val later = assertNotNull(
            container.challenges.create(
                ChallengeDraft(
                    title = "Later",
                    topicsInput = "Sport",
                    timing = Timing.Fixed(LocalDate(2026, 1, 10), LocalDate(2026, 1, 12)),
                    tasks = mapOf(null to listOf(TaskDraft("Go"))),
                ),
                anna.id,
            ),
        )
        assertEquals(listOf(later.id), stateOn(clock.today()).upcoming.map { it.challenge.id })
        assertTrue(stateOn(jan1).upcoming.isEmpty())
    }
}
