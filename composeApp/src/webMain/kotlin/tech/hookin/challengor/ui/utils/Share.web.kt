package tech.hookin.challengor.ui.utils

import androidx.compose.runtime.Composable

/** The browser build copies the link to the clipboard. */
@Composable
actual fun rememberShareSheet(): ((text: String) -> Unit)? = null
