package tech.hookin.challengor

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import tech.hookin.challengor.data.AppTheme

/** Launcher entries declared as `<activity-alias>` in the manifest, one per icon. */
private const val ALIAS_DARK = "tech.hookin.challengor.LauncherDark"
private const val ALIAS_LIGHT = "tech.hookin.challengor.LauncherLight"

/**
 * Shows the home-screen icon matching [theme] by enabling its launcher alias and
 * disabling the other one. Does nothing if the right icon is already active.
 *
 * Call it when the app goes to the background: launchers may restart the app
 * when its components change.
 */
fun applyLauncherIcon(context: Context, theme: AppTheme) {
    val pm = context.packageManager
    val (wanted, other) = when (theme) {
        AppTheme.LIGHT -> ALIAS_LIGHT to ALIAS_DARK
        AppTheme.DARK -> ALIAS_DARK to ALIAS_LIGHT
    }
    val wantedComponent = ComponentName(context, wanted)
    val otherComponent = ComponentName(context, other)
    if (pm.isEnabled(wantedComponent) && !pm.isEnabled(otherComponent)) return
    pm.setComponentEnabledSetting(
        wantedComponent,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP,
    )
    pm.setComponentEnabledSetting(
        otherComponent,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP,
    )
}

/** Whether [component] is enabled; "default" means what the manifest declares (only the dark alias). */
private fun PackageManager.isEnabled(component: ComponentName): Boolean =
    when (getComponentEnabledSetting(component)) {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> component.className == ALIAS_DARK
        else -> false
    }
