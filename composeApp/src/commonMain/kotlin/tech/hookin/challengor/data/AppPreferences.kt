package tech.hookin.challengor.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme { DARK, LIGHT }

/** Device-wide app settings, independent of who is logged in. */
class AppPreferences(private val settings: Settings) {

    private val _theme = MutableStateFlow(
        settings.getStringOrNull(KEY_THEME)
            ?.let { stored -> AppTheme.entries.find { it.name == stored } }
            ?: AppTheme.DARK,
    )
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        settings.putString(KEY_THEME, theme.name)
        _theme.value = theme
    }

    private companion object {
        const val KEY_THEME = "theme"
    }
}
