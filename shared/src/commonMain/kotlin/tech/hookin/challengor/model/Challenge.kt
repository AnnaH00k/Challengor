package tech.hookin.challengor.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Visibility {
    /** Listed in search and on the creator's profile. */
    PUBLIC,

    /** Only reachable through a shared link. */
    LINK_ONLY,
}

@Serializable
enum class ScheduleMode {
    /** The same task list repeats every day. */
    DAILY_REPEAT,

    /** Each day has its own to-do list. */
    DAY_PLAN,
}

/** When a challenge takes place. Chosen by the creator. */
@Serializable
sealed interface Timing {
    /** Every participant starts on the day they join and runs for [durationDays] days. */
    @Serializable
    @SerialName("relative")
    data class Relative(val durationDays: Int) : Timing

    /** Everyone runs the same calendar window, both dates inclusive. */
    @Serializable
    @SerialName("fixed")
    data class Fixed(val startDate: LocalDate, val endDate: LocalDate) : Timing
}

@Serializable
data class Challenge(
    val id: String,
    val creatorId: String,
    val title: String,
    val description: String,
    /** Normalized topic names, see [tech.hookin.challengor.domain.normalizeTopic]. */
    val topics: List<String>,
    val visibility: Visibility,
    val scheduleMode: ScheduleMode,
    val timing: Timing,
    val createdAt: Long,
    val updatedAt: Long,
    val deleted: Boolean = false,
)
