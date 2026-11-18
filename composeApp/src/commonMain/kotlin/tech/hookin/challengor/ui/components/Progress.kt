package tech.hookin.challengor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.hookin.challengor.domain.ChallengeProgress
import tech.hookin.challengor.domain.ProgressStatus
import tech.hookin.challengor.ui.utils.label

/** Circular progress with the percentage in the middle. */
@Composable
fun ProgressRing(
    progress: ChallengeProgress,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress.fraction },
            modifier = Modifier.size(size),
            color = colors.primary,
            trackColor = colors.outlineVariant,
            strokeWidth = size / 12,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
        )
        Text(
            text = "${progress.percent}%",
            style = if (size >= 80.dp) MaterialTheme.typography.titleLarge else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Small pill with the progress status. Success is filled, everything else outlined. */
@Composable
fun StatusBadge(status: ProgressStatus, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val success = status == ProgressStatus.COMPLETED_SUCCESS
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (success) colors.primary else colors.surface,
        contentColor = if (success) colors.onPrimary else colors.onSurface,
        border = if (success) null else BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
