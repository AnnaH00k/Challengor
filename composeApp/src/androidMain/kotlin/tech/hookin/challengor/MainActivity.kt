package tech.hookin.challengor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tech.hookin.challengor.data.AppPreferences
import tech.hookin.challengor.data.initAppContext
import tech.hookin.challengor.data.provideSettings
import tech.hookin.challengor.ui.theme.applySystemBars

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        initAppContext(this)
        // Right system bar icons from the first frame; PlatformThemeEffect updates them when the theme changes.
        applySystemBars(storedTheme())
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }

    override fun onStop() {
        super.onStop()
        // Swap the home-screen icon only while the app is in the background,
        // since the launcher may restart the app when it changes.
        applyLauncherIcon(this, storedTheme())
    }

    private fun storedTheme() = AppPreferences(provideSettings()).theme.value
}
