package tech.hookin.challengor.data

import com.russhwolf.settings.Settings

/** Platform key-value storage (SharedPreferences, NSUserDefaults, java.util.prefs, localStorage). */
expect fun provideSettings(): Settings
