package tech.hookin.challengor.ui.screens.explore

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import tech.hookin.challengor.data.AppContainer
import tech.hookin.challengor.data.ChallengeSort
import tech.hookin.challengor.data.LocalState
import tech.hookin.challengor.data.TopicCount
import tech.hookin.challengor.data.account
import tech.hookin.challengor.data.activeParticipation
import tech.hookin.challengor.data.participantCount
import tech.hookin.challengor.data.progressOf
import tech.hookin.challengor.data.searchChallenges
import tech.hookin.challengor.data.topicCounts
import tech.hookin.challengor.domain.ChallengeProgress
import tech.hookin.challengor.domain.normalizeTopic
import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.ui.uiStateOf

/** A challenge in a list, with what the viewer needs to decide whether to open it. */
data class ChallengeListItem(
    val challenge: Challenge,
    val creatorName: String?,
    val participantCount: Int,
    /** The viewer's own progress, if they take part. */
    val progress: ChallengeProgress?,
)

fun LocalState.listItem(challenge: Challenge, viewerId: String?, today: LocalDate) = ChallengeListItem(
    challenge = challenge,
    creatorName = account(challenge.creatorId)?.name,
    participantCount = participantCount(challenge.id),
    progress = viewerId?.let { activeParticipation(challenge.id, it) }?.let { progressOf(it, today) },
)

data class ExploreUiState(
    val query: String = "",
    val topic: String? = null,
    val sort: ChallengeSort = ChallengeSort.NEWEST,
    val topics: List<TopicCount> = emptyList(),
    val results: List<ChallengeListItem> = emptyList(),
)

class ExploreViewModel(
    private val container: AppContainer,
    private val userId: String,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val topic = MutableStateFlow<String?>(null)
    private val sort = MutableStateFlow(ChallengeSort.NEWEST)

    val uiState = uiStateOf(
        combine(container.store.state, query, topic, sort, ::build),
        build(container.store.current, "", null, ChallengeSort.NEWEST),
    )

    fun setQuery(value: String) {
        query.value = value
    }

    /** Selecting the active topic again clears the filter. */
    fun toggleTopic(value: String) {
        topic.value = value.takeIf { it != topic.value }
    }

    fun setSort(value: ChallengeSort) {
        sort.value = value
    }

    private fun build(state: LocalState, query: String, topic: String?, sort: ChallengeSort): ExploreUiState {
        val today = container.clock.today()
        val typed = normalizeTopic(query)
        // Typing narrows the topic chips, so users can search for a challenge type directly.
        val topics = state.topicCounts().filter { typed.isEmpty() || typed in it.topic || it.topic == topic }
        return ExploreUiState(
            query = query,
            topic = topic,
            sort = sort,
            topics = topics,
            results = state.searchChallenges(query, topic, sort).map { state.listItem(it, userId, today) },
        )
    }
}
