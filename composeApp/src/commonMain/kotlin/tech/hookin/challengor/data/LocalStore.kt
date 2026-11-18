package tech.hookin.challengor.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

/**
 * Holds the [LocalState] in memory, exposes it as a flow and persists every change.
 * On first start, data from the previous app version is migrated once.
 */
class LocalStore(
    settings: Settings,
    migration: LegacyMigration,
) {
    private val store = JsonSettingsStore(settings)

    private val _state = MutableStateFlow(
        store.load<LocalState>(KEY_STATE)
            ?: migration.migrate(store).also { store.save(KEY_STATE, it) },
    )
    val state: StateFlow<LocalState> = _state.asStateFlow()

    val current: LocalState get() = _state.value

    fun update(transform: (LocalState) -> LocalState) {
        val updated = _state.updateAndGet(transform)
        store.save(KEY_STATE, updated)
    }

    private companion object {
        const val KEY_STATE = "state_v2"
    }
}
