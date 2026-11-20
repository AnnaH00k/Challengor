package tech.hookin.challengor.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSLog
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationState
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.alternateIconName
import platform.UIKit.setAlternateIconName
import platform.UIKit.supportsAlternateIcons
import tech.hookin.challengor.data.AppTheme
import kotlin.coroutines.resume

/** Alternate icon declared in the asset catalog (`AppIconLight.appiconset`, `ASSETCATALOG_COMPILER_ALTERNATE_APPICON_NAMES`). */
private const val LIGHT_ICON = "AppIconLight"

@Composable
actual fun PlatformThemeEffect(theme: AppTheme) {
    LaunchedEffect(theme) {
        applyInterfaceStyle(theme)
        // iOS cancels icon changes unless the app is in the foreground (e.g. during launch).
        awaitAppActive()
        applyAppIcon(theme)
    }
}

/**
 * Pins the windows to light or dark mode to match the app theme, so the status bar
 * shows dark icons on the sage theme and light icons on the forest theme,
 * whatever the phone's own appearance setting is.
 */
private fun applyInterfaceStyle(theme: AppTheme) {
    val style = if (theme == AppTheme.LIGHT) UIUserInterfaceStyle.UIUserInterfaceStyleLight
    else UIUserInterfaceStyle.UIUserInterfaceStyleDark
    UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { it.windows.filterIsInstance<UIWindow>() }
        .forEach { it.overrideUserInterfaceStyle = style }
}

/** Returns once the app is active in the foreground; right away if it already is. */
private suspend fun awaitAppActive() {
    if (UIApplication.sharedApplication.applicationState == UIApplicationState.UIApplicationStateActive) return
    val center = NSNotificationCenter.defaultCenter
    suspendCancellableCoroutine { cont ->
        var observer: Any? = null
        observer = center.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ ->
            observer?.let(center::removeObserver)
            if (cont.isActive) cont.resume(Unit)
        }
        cont.invokeOnCancellation { observer?.let(center::removeObserver) }
    }
}

/**
 * Shows the home-screen icon matching [theme]: the light alternate icon for the sage theme,
 * the primary (forest) icon otherwise. Does nothing if the right icon is already set,
 * so iOS only shows its "icon changed" notice when the theme actually switches.
 */
private fun applyAppIcon(theme: AppTheme) {
    val app = UIApplication.sharedApplication
    if (!app.supportsAlternateIcons) return
    val wanted = if (theme == AppTheme.LIGHT) LIGHT_ICON else null
    if (app.alternateIconName == wanted) return
    app.setAlternateIconName(wanted) { error ->
        if (error != null) log("could not change app icon: ${error.localizedDescription}")
    }
}

/** NSLog with the message as plain text (Kotlin strings can't be passed as %@ arguments). */
private fun log(message: String) = NSLog("Challengor: " + message.replace("%", "%%"))
