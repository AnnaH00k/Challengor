package tech.hookin.challengor.domain

import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.model.ScheduleMode
import tech.hookin.challengor.model.TaskTemplate
import tech.hookin.challengor.model.Timing
import tech.hookin.challengor.model.Visibility
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChallengeDraftTest {
    private val valid = ChallengeDraft(
        title = "Run",
        topicsInput = "Sport",
        tasks = mapOf(null to listOf(TaskDraft("Run 5 km"))),
    )

    private val writing = valid.copy(
        title = "Write",
        timing = Timing.Relative(100),
        tasks = mapOf(null to listOf(TaskDraft("Write", measured = true, target = "1000", unit = " words "))),
    )

    @Test
    fun validDraft() {
        assertNull(valid.validate())
        assertNull(writing.validate())
    }

    @Test
    fun errorsInOrder() {
        assertEquals("Please enter a title", valid.copy(title = " ").validate())
        assertEquals("Add at least one topic", valid.copy(topicsInput = " , ").validate())
        assertEquals("Add at least one task", valid.copy(tasks = mapOf(null to listOf(TaskDraft("  ")))).validate())
        assertEquals(Timing.Relative(0).validate(), valid.copy(timing = Timing.Relative(0)).validate())
    }

    @Test
    fun measuredTasksNeedTargetAndUnit() {
        fun with(task: TaskDraft) = writing.copy(tasks = mapOf(null to listOf(task))).validate()
        assertEquals("The daily target must be a positive number", with(TaskDraft("Write", measured = true, target = "", unit = "words")))
        assertEquals("The daily target must be a positive number", with(TaskDraft("Write", measured = true, target = "0", unit = "words")))
        assertEquals("Please enter a unit, e.g. words or km", with(TaskDraft("Write", measured = true, target = "1000", unit = " ")))
        assertNull(with(TaskDraft("Write", measured = false, target = "abc")), "fields of unmeasured tasks are ignored")
    }

    @Test
    fun goalIsDailyTargetTimesDays() {
        assertEquals(mapOf("words" to 100_000L), writing.goals)

        val plan = writing.copy(
            scheduleMode = ScheduleMode.DAY_PLAN,
            timing = Timing.Relative(3),
            tasks = mapOf(
                0 to listOf(TaskDraft("Run", measured = true, target = "5", unit = "km")),
                2 to listOf(TaskDraft("Run", measured = true, target = "8", unit = "km")),
            ),
        )
        assertEquals(mapOf("km" to 13L), plan.goals)
    }

    @Test
    fun draftFromAnExistingChallengeRoundTrips() {
        val challenge = Challenge(
            id = "c1", creatorId = "u1", title = "Write", description = "Daily", topics = listOf("writing", "books"),
            visibility = Visibility.LINK_ONLY, scheduleMode = ScheduleMode.DAILY_REPEAT,
            timing = Timing.Relative(100), createdAt = 0, updatedAt = 0,
        )
        val tasks = listOf(
            TaskTemplate("t2", "c1", null, "Read", 1, 0),
            TaskTemplate("t1", "c1", null, "Write", 0, 0, target = 1000, unit = "words"),
            TaskTemplate("t3", "c1", null, "Old", 2, 0, deleted = true),
        )
        val draft = ChallengeDraft.from(challenge, tasks)
        assertEquals("writing, books", draft.topicsInput)
        assertEquals(
            listOf(TaskDraft("Write", true, "1000", "words", "t1"), TaskDraft("Read", id = "t2")),
            draft.tasks.getValue(null),
        )
        assertNull(draft.validate())
        assertEquals(mapOf("words" to 100_000L), draft.goals)
    }

    @Test
    fun cleanTasksKeepOnlyTasksOfTheChosenMode() {
        val draft = valid.copy(
            scheduleMode = ScheduleMode.DAY_PLAN,
            timing = Timing.Relative(2),
            tasks = mapOf(
                null to listOf(TaskDraft("daily")),
                0 to listOf(TaskDraft(" a "), TaskDraft("")),
                1 to emptyList(),
                5 to listOf(TaskDraft("too late")),
            ),
        )
        assertEquals(mapOf<Int?, List<TaskDraft>>(0 to listOf(TaskDraft("a"))), draft.cleanTasks)
        assertEquals(mapOf<Int?, List<TaskDraft>>(null to listOf(TaskDraft("Run 5 km"))), valid.cleanTasks)
    }
}
