package tech.hookin.challengor.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import tech.hookin.challengor.domain.ChallengeDraft
import tech.hookin.challengor.domain.ProgressStatus
import tech.hookin.challengor.domain.TaskDraft
import tech.hookin.challengor.model.ScheduleMode
import tech.hookin.challengor.model.Timing
import tech.hookin.challengor.model.Visibility
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChallengeRepositoryTest {
    private val clock = FakeClock()
    private val container = testContainer(clock = clock)
    private val anna = assertNotNull(container.auth.register("Anna", "anna@example.com", "secret1"))
    private val ben = assertNotNull(container.auth.register("Ben", "ben@example.com", "secret1"))
    private val repo = container.challenges
    private val state get() = container.store.current

    private val runDraft = ChallengeDraft(
        title = " Run ",
        topicsInput = "Sport, Health",
        timing = Timing.Relative(3),
        tasks = mapOf(null to listOf(TaskDraft("Run 5 km"), TaskDraft("Stretch"))),
    )

    @Test
    fun createSavesTasksAndLetsTheCreatorJoin() {
        val challenge = assertNotNull(repo.create(runDraft, anna.id))
        assertEquals("Run", challenge.title)
        assertEquals(listOf("sport", "health"), challenge.topics)
        assertEquals(2, state.tasksOf(challenge.id).size)
        val participation = assertNotNull(state.activeParticipation(challenge.id, anna.id))
        assertEquals(clock.date, participation.startDate)
        assertEquals(clock.date.plus(2, DateTimeUnit.DAY), participation.endDate)
    }

    @Test
    fun invalidDraftIsNotSaved() {
        assertNull(repo.create(runDraft.copy(tasks = emptyMap()), anna.id))
        assertTrue(state.challenges.isEmpty())
    }

    @Test
    fun joiningTwiceKeepsOneParticipation() {
        val challenge = assertNotNull(repo.create(runDraft, anna.id))
        val first = repo.join(challenge.id, ben.id)
        assertEquals(first, repo.join(challenge.id, ben.id))
        assertEquals(2, state.participantCount(challenge.id))
    }

    @Test
    fun fixedChallengeCannotBeJoinedAfterItEnded() {
        val fixed = runDraft.copy(timing = Timing.Fixed(clock.date, clock.date.plus(1, DateTimeUnit.DAY)))
        val challenge = assertNotNull(repo.create(fixed, anna.id))
        clock.advanceDays(1)
        assertEquals(clock.date, repo.join(challenge.id, ben.id)?.startDate, "late joiner starts today")
        clock.advanceDays(1)
        assertNull(repo.join(challenge.id, assertNotNull(container.auth.register("Cleo", "c@example.com", "secret1")).id))
    }

    @Test
    fun tickingTasksDrivesProgressToSuccess() {
        val challenge = assertNotNull(repo.create(runDraft, anna.id))
        val participation = assertNotNull(state.activeParticipation(challenge.id, anna.id))
        val tasks = state.tasksOf(challenge.id)

        assertFalse(repo.setTaskDone(participation.id, tasks[0].id, clock.date.plus(1, DateTimeUnit.DAY), true), "future day")

        repeat(3) { day ->
            tasks.forEach { assertTrue(repo.setTaskDone(participation.id, it.id, clock.date, true)) }
            if (day < 2) clock.advanceDays(1)
        }
        val progress = assertNotNull(state.progressOf(participation, clock.date))
        assertEquals(100, progress.percent)
        assertEquals(ProgressStatus.COMPLETED_SUCCESS, progress.status)

        assertTrue(repo.setTaskDone(participation.id, tasks[0].id, clock.date, false), "untick")
        assertEquals(83, state.progressOf(participation, clock.date)?.percent)
    }

    @Test
    fun loggedAmountsCanBeCorrectedAndMadeUp() {
        val writing = runDraft.copy(
            title = "Write",
            tasks = mapOf(null to listOf(TaskDraft("Write", measured = true, target = "1000", unit = "words"))),
        )
        val challenge = assertNotNull(repo.create(writing, anna.id))
        val task = state.tasksOf(challenge.id).single()
        assertEquals(1000, task.target)
        assertEquals("words", task.unit)
        val participation = assertNotNull(state.activeParticipation(challenge.id, anna.id))

        assertTrue(repo.logAmount(participation.id, task.id, clock.date, 600))
        assertEquals(-400, state.progressOf(participation, clock.date)?.goals?.single()?.aheadBy)

        assertTrue(repo.logAmount(participation.id, task.id, clock.date, 1600), "a day's entry is replaced, not added")
        assertEquals(600, state.progressOf(participation, clock.date)?.goals?.single()?.aheadBy)
        assertEquals(1, state.completions.size)

        assertFalse(repo.logAmount(participation.id, task.id, clock.date.plus(1, DateTimeUnit.DAY), 500), "future day")
        clock.advanceDays(1)
        assertTrue(repo.logAmount(participation.id, task.id, clock.date, 1400))
        assertTrue(repo.logAmount(participation.id, task.id, clock.date.plus(-1, DateTimeUnit.DAY), 1600), "earlier day while running")
        assertEquals(100, state.progressOf(participation, clock.date)?.percent)
    }

    @Test
    fun editingKeepsEntriesOfKeptTasks() {
        val challenge = assertNotNull(repo.create(runDraft, anna.id))
        val participation = assertNotNull(state.activeParticipation(challenge.id, anna.id))
        val (run, stretch) = state.tasksOf(challenge.id)
        repo.setTaskDone(participation.id, run.id, clock.date, true)

        // Turn "Run 5 km" into a measured task, drop "Stretch", add "Drink water".
        val draft = ChallengeDraft.from(state.challenge(challenge.id)!!, state.tasks).let { d ->
            val tasks = d.tasks.getValue(null)
            d.copy(
                title = "Run more",
                tasks = mapOf(
                    null to listOf(
                        tasks[0].copy(title = "Run", measured = true, target = "5", unit = "km"),
                        TaskDraft("Drink water"),
                    ),
                ),
            )
        }
        assertNotNull(repo.update(challenge.id, draft, anna.id))

        assertEquals("Run more", state.challenge(challenge.id)?.title)
        val tasks = state.tasksOf(challenge.id)
        assertEquals(listOf("Run", "Drink water"), tasks.sortedBy { it.position }.map { it.title })
        assertEquals(run.id, tasks.first { it.title == "Run" }.id, "kept task keeps its id")
        assertEquals(5, tasks.first { it.title == "Run" }.target)
        assertTrue(state.tasks.single { it.id == stretch.id }.deleted)
        assertEquals(1, state.completions.count { it.taskTemplateId == run.id }, "entries are kept")
    }

    @Test
    fun onlyTheCreatorCanEdit() {
        val challenge = assertNotNull(repo.create(runDraft, anna.id))
        assertNull(repo.update(challenge.id, runDraft.copy(title = "Hacked"), ben.id))
        assertNull(repo.update(challenge.id, runDraft.copy(title = " "), anna.id), "invalid draft")
        assertEquals("Run", state.challenge(challenge.id)?.title)
    }

    @Test
    fun changingTheLengthMovesRunningParticipations() {
        val challenge = assertNotNull(repo.create(runDraft, anna.id)) // 3 days
        clock.advanceDays(1)
        val bens = assertNotNull(repo.join(challenge.id, ben.id))

        repo.update(challenge.id, runDraft.copy(timing = Timing.Relative(10)), anna.id)
        val moved = state.participations.single { it.id == bens.id }
        assertEquals(bens.startDate, moved.startDate)
        assertEquals(bens.startDate.plus(9, DateTimeUnit.DAY), moved.endDate)

        val start = clock.date.plus(2, DateTimeUnit.DAY)
        repo.update(challenge.id, runDraft.copy(timing = Timing.Fixed(start, start.plus(4, DateTimeUnit.DAY))), anna.id)
        val fixed = state.participations.single { it.id == bens.id }
        assertEquals(start, fixed.startDate, "nobody starts before the new fixed start")
        assertEquals(start.plus(4, DateTimeUnit.DAY), fixed.endDate)
    }

    @Test
    fun leaveAndRejoinStartsFresh() {
        val challenge = assertNotNull(repo.create(runDraft, anna.id))
        repo.leave(challenge.id, anna.id)
        assertNull(state.activeParticipation(challenge.id, anna.id))
        assertEquals(ProgressStatus.ABANDONED, state.progressOf(state.latestParticipationsOf(anna.id).single(), clock.date)?.status)

        clock.advanceDays(1)
        val again = assertNotNull(repo.join(challenge.id, anna.id))
        assertEquals(clock.date, again.startDate)
        assertEquals(again, state.latestParticipationsOf(anna.id).single())
    }

    @Test
    fun onlyTheCreatorCanDelete() {
        val challenge = assertNotNull(repo.create(runDraft, anna.id))
        assertFalse(repo.delete(challenge.id, ben.id))
        assertTrue(repo.delete(challenge.id, anna.id))
        assertNull(state.challenge(challenge.id))
        assertTrue(state.latestParticipationsOf(anna.id).isEmpty())
    }

    @Test
    fun dayPlanChecklistFollowsTheDays() {
        val plan = runDraft.copy(
            scheduleMode = ScheduleMode.DAY_PLAN,
            tasks = mapOf(0 to listOf(TaskDraft("Warm up")), 2 to listOf(TaskDraft("Race"), TaskDraft("Celebrate"))),
        )
        val challenge = assertNotNull(repo.create(plan, anna.id))
        val participation = assertNotNull(state.activeParticipation(challenge.id, anna.id))
        val progress = assertNotNull(state.progressOf(participation, clock.date))
        assertEquals(listOf(1, 0, 2), progress.days.map { it.total })
    }

    @Test
    fun searchFiltersAndSorts() {
        val run = assertNotNull(repo.create(runDraft, anna.id))
        clock.advanceDays(1)
        val read = assertNotNull(repo.create(runDraft.copy(title = "Read", topicsInput = "books"), ben.id))
        repo.create(runDraft.copy(title = "Secret", visibility = Visibility.LINK_ONLY), anna.id)
        repo.join(run.id, ben.id)

        assertEquals(listOf(read.id, run.id), state.searchChallenges("", null, ChallengeSort.NEWEST).map { it.id }, "link-only is hidden")
        assertEquals(listOf(run.id, read.id), state.searchChallenges("", null, ChallengeSort.POPULAR).map { it.id })
        assertEquals(listOf(read.id), state.searchChallenges("BOOK", null, ChallengeSort.TITLE).map { it.id }, "matches topics")
        assertEquals(listOf(run.id), state.searchChallenges("", "health", ChallengeSort.TITLE).map { it.id })
        assertEquals(listOf("books", "health", "sport"), state.topicCounts().map { it.topic })
        assertEquals(2, state.createdBy(anna.id, viewerId = anna.id).size)
        assertEquals(1, state.createdBy(anna.id, viewerId = ben.id).size)
    }
}

