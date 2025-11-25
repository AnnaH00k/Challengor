package tech.hookin.learningkmp.storage

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

private lateinit var appContext: Context

fun initAppContext(context: Context) {
    appContext = context.applicationContext
}

actual fun provideSettings(): Settings {
    return SharedPreferencesSettings(appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
}
