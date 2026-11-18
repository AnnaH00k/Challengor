package tech.hookin.challengor.ui.utils

import androidx.compose.runtime.Composable

/** Desktop has no share sheet; the link is copied to the clipboard. */
@Composable
actual fun rememberShareSheet(): ((text: String) -> Unit)? = null
