package tech.hookin.challengor.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * What a participant did for a task on a given day. Unticking keeps the row with
 * [done] = false, so the change syncs like any other update.
 */
@Serializable
data class TaskCompletion(
    val participationId: String,
    val taskTemplateId: String,
    val date: LocalDate,
    val done: Boolean,
    val updatedAt: Long,
    /** Logged amount of a measurable task ([TaskTemplate.target] set); `null` for checkbox tasks. */
    val amount: Int? = null,
) {
    /** Deterministic id: there is at most one completion per participation, task and day. */
    val id: String get() = completionId(participationId, taskTemplateId, date)
}

fun completionId(participationId: String, taskTemplateId: String, date: LocalDate): String =
    "$participationId:$taskTemplateId:$date"
