package tech.hookin.challengor.ui.screens.mychallenges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.hookin.challengor.ui.components.ChallengeCard
import tech.hookin.challengor.ui.components.ContentList
import tech.hookin.challengor.ui.components.EmptyState
import tech.hookin.challengor.ui.components.chipColors

private enum class Filter(val label: String) { ACTIVE("Running"), FINISHED("Finished"), CREATED("Created by me") }

@Composable
fun MyChallengesScreen(
    viewModel: MyChallengesViewModel,
    onOpenChallenge: (String) -> Unit,
    onCreate: () -> Unit,
    onExplore: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf(Filter.ACTIVE) }
    val items = when (filter) {
        Filter.ACTIVE -> state.active
        Filter.FINISHED -> state.finished
        Filter.CREATED -> state.created
    }

    ContentList {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Filter.entries.forEach { entry ->
                    val count = when (entry) {
                        Filter.ACTIVE -> state.active.size
                        Filter.FINISHED -> state.finished.size
                        Filter.CREATED -> state.created.size
                    }
                    FilterChip(
                        selected = entry == filter,
                        onClick = { filter = entry },
                        label = { Text("${entry.label} ($count)") },
                        colors = chipColors(),
                    )
                }
            }
        }

        if (items.isEmpty()) {
            item {
                when (filter) {
                    Filter.ACTIVE -> EmptyState("No running challenges.", actionLabel = "Explore challenges", onAction = onExplore)
                    Filter.FINISHED -> EmptyState("Finished challenges and your results show up here.")
                    Filter.CREATED -> EmptyState("You haven't created a challenge yet.", actionLabel = "Create one", onAction = onCreate)
                }
            }
        }

        items(items, key = { it.challenge.id }) { item ->
            ChallengeCard(
                challenge = item.challenge,
                creatorName = item.creatorName,
                participantCount = item.participantCount,
                progress = item.progress,
                onClick = { onOpenChallenge(item.challenge.id) },
            )
        }
    }
}
