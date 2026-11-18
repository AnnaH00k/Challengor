package tech.hookin.challengor.ui.utils

import androidx.compose.runtime.Composable

// TODO(iOS): present a UIActivityViewController; until then the link is copied to the clipboard.
@Composable
actual fun rememberShareSheet(): ((text: String) -> Unit)? = null
