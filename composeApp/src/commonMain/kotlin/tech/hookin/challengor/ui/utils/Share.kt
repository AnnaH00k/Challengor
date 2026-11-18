package tech.hookin.challengor.ui.utils

import androidx.compose.runtime.Composable

/** Public web address of the app (GitHub Pages). Shared links open the challenge there. */
const val WEB_APP_URL = "https://annah00k.github.io/Challengor/"

private const val CHALLENGE_LINK_PREFIX = "#/challenge/"

fun challengeLink(challengeId: String): String = WEB_APP_URL + CHALLENGE_LINK_PREFIX + challengeId

/** Extracts the challenge id from a URL fragment such as `#/challenge/<id>`. */
fun challengeIdFromFragment(fragment: String): String? =
    fragment.takeIf { it.startsWith(CHALLENGE_LINK_PREFIX) }
        ?.removePrefix(CHALLENGE_LINK_PREFIX)
        ?.takeIf { it.isNotBlank() }

/**
 * The platform's native share sheet, or `null` where there is none
 * (callers then copy the link to the clipboard instead).
 */
@Composable
expect fun rememberShareSheet(): ((text: String) -> Unit)?
