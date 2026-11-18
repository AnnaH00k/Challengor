package tech.hookin.challengor.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tech.hookin.challengor.data.AppContainer
import tech.hookin.challengor.data.LocalState
import tech.hookin.challengor.data.LocalStore

/** Gives screens access to the repositories without passing them through every call. */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> { error("No AppContainer provided") }

/** Maps the local state to a screen's UI state and keeps it up to date. */
fun <T> ViewModel.uiStateOf(store: LocalStore, build: (LocalState) -> T): StateFlow<T> =
    store.state.map(build).stateIn(viewModelScope, SharingStarted.Eagerly, build(store.current))

/** Like [uiStateOf], for UI state that also depends on screen-local inputs such as a search query. */
fun <T> ViewModel.uiStateOf(flow: Flow<T>, initial: T): StateFlow<T> =
    flow.stateIn(viewModelScope, SharingStarted.Eagerly, initial)
