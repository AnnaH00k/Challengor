package tech.hookin.challengor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.hookin.challengor.domain.ChallengeProgress
import tech.hookin.challengor.model.Challenge
import tech.hookin.challengor.ui.utils.label
import tech.hookin.challengor.ui.utils.plural
import tech.hookin.challengor.ui.theme.neumorphic
import tech.hookin.challengor.ui.theme.OnCard

/** List entry for a challenge, optionally with the viewer's own progress. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChallengeCard(
    challenge: Challenge,
    creatorName: String?,
    participantCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: ChallengeProgress? = null,
) {
    OnCard {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().neumorphic(CardDefaults.shape),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (challenge.description.isNotBlank()) {
                        Text(
                            text = challenge.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = listOfNotNull(
                            challenge.scheduleMode.label,
                            plural(participantCount, "participant"),
                            creatorName?.let { "by $it" },
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        challenge.topics.forEach { TopicTag(it) }
                        progress?.let { StatusBadge(it.status) }
                    }
                }
                progress?.let { ProgressRing(it) }
            }
        }
    }
}
