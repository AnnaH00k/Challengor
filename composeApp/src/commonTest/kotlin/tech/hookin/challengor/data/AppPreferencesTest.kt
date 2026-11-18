package tech.hookin.challengor.data

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class AppPreferencesTest {
    private val settings = MapSettings()

    @Test
    fun darkIsTheDefault() {
        assertEquals(AppTheme.DARK, AppPreferences(settings).theme.value)
    }

    @Test
    fun chosenThemeIsKeptAcrossRestarts() {
        val preferences = AppPreferences(settings)
        preferences.setTheme(AppTheme.LIGHT)
        assertEquals(AppTheme.LIGHT, preferences.theme.value)
        assertEquals(AppTheme.LIGHT, AppPreferences(settings).theme.value, "read back after restart")
    }

    @Test
    fun unknownStoredValueFallsBackToDark() {
        settings.putString("theme", "PURPLE")
        assertEquals(AppTheme.DARK, AppPreferences(settings).theme.value)
    }
}
