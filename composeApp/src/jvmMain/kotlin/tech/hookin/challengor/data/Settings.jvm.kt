package tech.hookin.challengor.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

actual fun provideSettings(): Settings {
    val prefs = Preferences.userRoot().node("tech.hookin.challengor")
    return PreferencesSettings(prefs)
}