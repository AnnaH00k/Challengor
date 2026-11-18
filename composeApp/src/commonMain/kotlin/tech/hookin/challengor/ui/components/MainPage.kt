package tech.hookin.challengor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.hookin.challengor.ui.utils.isWideScreen


/** Root layout of the login, register and landing pages: themed background, safe-area insets and scrolling. */
@Composable
fun MainPage(content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val wide = isWideScreen()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides colors.onBackground,
            LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(color = colors.onBackground),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = MaxContentWidth)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = if (wide) 16.dp else 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}
