package tech.hookin.challengor.ui.screens.people

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import tech.hookin.challengor.data.AppContainer
import tech.hookin.challengor.data.LocalState
import tech.hookin.challengor.data.createdBy
import tech.hookin.challengor.data.latestParticipationsOf
import tech.hookin.challengor.data.searchUsers
import tech.hookin.challengor.model.UserProfile
import tech.hookin.challengor.ui.uiStateOf

data class PersonItem(
    val profile: UserProfile,
    val createdCount: Int,
    val joinedCount: Int,
    val isMe: Boolean,
)

data class PeopleUiState(
    val query: String = "",
    val people: List<PersonItem> = emptyList(),
)

class PeopleViewModel(
    container: AppContainer,
    private val userId: String,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val uiState = uiStateOf(
        combine(container.store.state, query, ::build),
        build(container.store.current, ""),
    )

    fun setQuery(value: String) {
        query.value = value
    }

    private fun build(state: LocalState, query: String) = PeopleUiState(
        query = query,
        people = state.searchUsers(query).map { account ->
            PersonItem(
                profile = account.toProfile(),
                createdCount = state.createdBy(account.id, viewerId = userId).size,
                joinedCount = state.latestParticipationsOf(account.id).count { !it.left },
                isMe = account.id == userId,
            )
        },
    )
}
