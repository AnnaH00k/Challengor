package tech.hookin.challengor.ui.screens.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.SortAscending
import tech.hookin.challengor.data.ChallengeSort
import tech.hookin.challengor.ui.components.ChallengeCard
import tech.hookin.challengor.ui.components.ContentList
import tech.hookin.challengor.ui.components.EmptyState
import tech.hookin.challengor.ui.components.SearchField
import tech.hookin.challengor.ui.components.chipColors
import tech.hookin.challengor.ui.utils.plural

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onOpenChallenge: (String) -> Unit,
    onCreate: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ContentList {
        item {
            SearchField(
                query = state.query,
                onQueryChange = viewModel::setQuery,
                placeholder = "Search challenges or topics",
            )
        }

        if (state.topics.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Topics", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.topics, key = { it.topic }) { (topic, count) ->
                            FilterChip(
                                selected = topic == state.topic,
                                onClick = { viewModel.toggleTopic(topic) },
                                label = { Text("#$topic · $count") },
                                colors = chipColors(),
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(PhosphorIcons.Regular.SortAscending, contentDescription = "Sort")
                ChallengeSort.entries.forEach { sort ->
                    FilterChip(
                        selected = sort == state.sort,
                        onClick = { viewModel.setSort(sort) },
                        label = { Text(sort.label) },
                        colors = chipColors(),
                    )
                }
            }
        }

        item {
            Text(
                text = plural(state.results.size, "challenge") + (state.topic?.let { " in #$it" } ?: ""),
                style = MaterialTheme.typography.labelMedium,
            )
        }

        if (state.results.isEmpty()) {
            item {
                EmptyState(
                    text = "No public challenges found.",
                    actionLabel = "Create a challenge",
                    onAction = onCreate,
                )
            }
        }

        items(state.results, key = { it.challenge.id }) { item ->
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

