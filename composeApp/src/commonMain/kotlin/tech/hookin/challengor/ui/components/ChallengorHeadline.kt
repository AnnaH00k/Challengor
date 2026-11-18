package tech.hookin.challengor.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import tech.hookin.challengor.resources.Res
import tech.hookin.challengor.resources.challengor_headline

/** "Challengor" word mark, tinted to the current text colour. */
@Composable
fun ChallengorHeadline(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.challengor_headline),
        contentDescription = "Challengor",
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
        modifier = modifier.widthIn(max = 600.dp),
    )
}
