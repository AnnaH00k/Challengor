package tech.hookin.challengor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val borderColor = if (enabled) colors.outline else colors.outline.copy(alpha = 0.3f)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.padding(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.surface,
            contentColor = colors.onSurface,
            disabledContainerColor = colors.surface.copy(alpha = 0.4f),
            disabledContentColor = colors.onSurface.copy(alpha = 0.4f),
        ),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(text = text, maxLines = 1)
    }
}
