package tech.hookin.challengor.ui.screens.people

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.hookin.challengor.ui.components.Avatar
import tech.hookin.challengor.ui.components.ContentList
import tech.hookin.challengor.ui.components.EmptyState
import tech.hookin.challengor.ui.components.SearchField
import tech.hookin.challengor.ui.utils.plural
import tech.hookin.challengor.ui.theme.neumorphic
import tech.hookin.challengor.ui.theme.OnSage

@Composable
fun PeopleScreen(
    viewModel: PeopleViewModel,
    onOpenUser: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ContentList {
        item { SearchField(query = state.query, onQueryChange = viewModel::setQuery, placeholder = "Search people") }

        if (state.people.isEmpty()) {
            item { EmptyState("Nobody found.") }
        }

        items(state.people, key = { it.profile.id }) { person ->
            OnSage {
                Card(
                    onClick = { onOpenUser(person.profile.id) },
                    modifier = Modifier.fillMaxWidth().neumorphic(CardDefaults.shape),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Avatar(person.profile.name)
                        Column {
                            Text(
                                text = person.profile.name + if (person.isMe) " (you)" else "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "${plural(person.createdCount, "challenge")} created · ${person.joinedCount} joined",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
