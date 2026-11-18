package tech.hookin.challengor.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ShieldCheck
import com.adamglin.phosphoricons.regular.SignOut
import tech.hookin.challengor.ui.components.Avatar
import tech.hookin.challengor.ui.components.ChallengeCard
import tech.hookin.challengor.ui.components.ContentList
import tech.hookin.challengor.ui.components.EmptyState
import tech.hookin.challengor.ui.components.SectionTitle
import tech.hookin.challengor.ui.screens.explore.ChallengeListItem
import tech.hookin.challengor.ui.utils.formatDate
import tech.hookin.challengor.ui.theme.neumorphic
import tech.hookin.challengor.ui.theme.OnSage

/**
 * A user's profile: who they are, their numbers and their challenges.
 * On the own profile it also offers logout and, for admins, the admin board.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onOpenChallenge: (String) -> Unit,
    onOpenAdmin: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = state.profile ?: run {
        ContentList { item { EmptyState("This user doesn't exist.") } }
        return
    }

    ContentList {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Avatar(profile.name, size = 80.dp)
                Text(profile.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                profile.email?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                Text("Member since ${formatDate(profile.createdAt)}", style = MaterialTheme.typography.labelMedium)
                if (profile.isAdmin) Text("Admin", style = MaterialTheme.typography.labelMedium)
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Stat("Created", state.challenges.created.size, Modifier.weight(1f))
                Stat("Running", state.challenges.active.size, Modifier.weight(1f))
                Stat("100 % done", state.completedCount, Modifier.weight(1f))
            }
        }

        if (state.isMe && (onOpenAdmin != null || onLogout != null)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                ) {
                    if (profile.isAdmin && onOpenAdmin != null) {
                        OutlinedButton(onClick = onOpenAdmin) {
                            Icon(PhosphorIcons.Regular.ShieldCheck, contentDescription = null)
                            Text("Admin board", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    if (onLogout != null) {
                        OutlinedButton(onClick = onLogout) {
                            Icon(PhosphorIcons.Regular.SignOut, contentDescription = null)
                            Text("Log out", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }

        challengeSection("Taking part", state.challenges.active, onOpenChallenge)
        challengeSection("Finished", state.challenges.finished, onOpenChallenge)
        challengeSection("Created", state.challenges.created, onOpenChallenge)
    }
}

private fun LazyListScope.challengeSection(
    title: String,
    items: List<ChallengeListItem>,
    onOpenChallenge: (String) -> Unit,
) {
    if (items.isEmpty()) return
    item(key = "title-$title") { SectionTitle(title) }
    items(items, key = { "$title-${it.challenge.id}" }) { item ->
        ChallengeCard(
            challenge = item.challenge,
            creatorName = item.creatorName,
            participantCount = item.participantCount,
            progress = item.progress,
            onClick = { onOpenChallenge(item.challenge.id) },
        )
    }
}

@Composable
private fun Stat(label: String, value: Int, modifier: Modifier = Modifier) {
    OnSage {
        Card(
            modifier = modifier.neumorphic(CardDefaults.shape),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(value.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}
