package tech.hookin.challengor.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SerializationTest {

    @Test
    fun challengeRoundTripsWithBothTimings() {
        val relative = Challenge(
            id = newId(), creatorId = "u1", title = "Run", description = "5 km", topics = listOf("sport"),
            visibility = Visibility.PUBLIC, scheduleMode = ScheduleMode.DAILY_REPEAT,
            timing = Timing.Relative(30), createdAt = 1, updatedAt = 2,
        )
        val fixed = relative.copy(timing = Timing.Fixed(LocalDate(2026, 1, 1), LocalDate(2026, 1, 31)))

        for (challenge in listOf(relative, fixed)) {
            val json = Json.encodeToString(challenge)
            assertEquals(challenge, Json.decodeFromString<Challenge>(json))
        }
        assertTrue("\"2026-01-01\"" in Json.encodeToString(fixed), "dates are ISO strings")
    }

    @Test
    fun completionIdIsDeterministic() {
        val c = TaskCompletion("p1", "t1", LocalDate(2026, 1, 1), done = true, updatedAt = 0)
        assertEquals("p1:t1:2026-01-01", c.id)
        assertEquals(c.id, c.copy(done = false, updatedAt = 5).id)
    }

    @Test
    fun dataWithoutAmountFieldsStillLoads() {
        val task = Json.decodeFromString<TaskTemplate>(
            """{"id":"t1","challengeId":"c1","dayIndex":null,"title":"Run","position":0,"updatedAt":0}""",
        )
        assertEquals(null, task.target)
        assertEquals(false, task.isMeasurable)
        val completion = Json.decodeFromString<TaskCompletion>(
            """{"participationId":"p1","taskTemplateId":"t1","date":"2026-01-01","done":true,"updatedAt":0}""",
        )
        assertEquals(null, completion.amount)
    }

    @Test
    fun newIdsAreUnique() {
        assertNotEquals(newId(), newId())
        assertEquals(36, newId().length)
    }
}
