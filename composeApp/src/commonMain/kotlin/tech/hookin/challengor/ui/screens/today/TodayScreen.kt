package tech.hookin.challengor.ui.screens.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CheckCircle
import tech.hookin.challengor.ui.components.ContentList
import tech.hookin.challengor.ui.components.EmptyState
import tech.hookin.challengor.ui.components.ProgressRing
import tech.hookin.challengor.ui.components.SectionTitle
import tech.hookin.challengor.ui.components.GoalSummary
import tech.hookin.challengor.ui.utils.long
import tech.hookin.challengor.ui.theme.neumorphic
import tech.hookin.challengor.ui.theme.OnCard

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onOpenChallenge: (String) -> Unit,
    onCreate: () -> Unit,
    onExplore: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ContentList {
        if (state.isEmpty) {
            item {
                EmptyState(text = "You are not taking part in any challenge yet.")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                ) {
                    OutlinedButton(onClick = onExplore) { Text("Explore challenges") }
                    OutlinedButton(onClick = onCreate) { Text("Create one") }
                }
            }
            return@ContentList
        }

        if (state.tasksTotal > 0) {
            item { DayProgress(state) }
        }

        if (state.active.isEmpty()) {
            item { EmptyState(text = "Nothing scheduled on this day.") }
        }

        items(state.active, key = { it.participation.id }) { item ->
            ChecklistCard(
                item = item,
                isToday = state.isTodaySelected,
                onOpen = { onOpenChallenge(item.challenge.id) },
                onToggle = { taskId, done -> viewModel.setDone(item.participation.id, taskId, item.date, done) },
                onLogAmount = { taskId, amount -> viewModel.logAmount(item.participation.id, taskId, item.date, amount) },
            )
        }

        if (state.upcoming.isNotEmpty()) {
            item { SectionTitle("Starting soon") }
            items(state.upcoming, key = { "upcoming-" + it.participation.id }) { item ->
                ChecklistCard(
                    item = item,
                    isToday = false,
                    onOpen = { onOpenChallenge(item.challenge.id) },
                    onToggle = { _, _ -> },
                    onLogAmount = { _, _ -> },
                    subtitle = "Starts ${item.participation.startDate.long()}",
                )
            }
        }
    }
}

@Composable
private fun DayProgress(state: TodayUiState) {
    val suffix = when {
        state.isTodaySelected -> " today"
        state.selectedDate > state.today -> " – upcoming"
        else -> ""
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${state.tasksDone} of ${state.tasksTotal} tasks done$suffix")
        LinearProgressIndicator(
            progress = { state.tasksDone.toFloat() / state.tasksTotal },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun ChecklistCard(
    item: DayChecklist,
    isToday: Boolean,
    onOpen: () -> Unit,
    onToggle: (taskId: String, done: Boolean) -> Unit,
    onLogAmount: (taskId: String, amount: Int) -> Unit,
    subtitle: String? = null,
) {
    OnCard {
        Card(
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth().neumorphic(CardDefaults.shape),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.challenge.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = subtitle
                                ?: "Day ${item.dayNumber} of ${item.progress.days.size} · on track ${item.progress.onTrackPercent}%",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    ProgressRing(item.progress, size = 48.dp)
                }

                item.progress.goals.forEach { GoalSummary(it, modifier = Modifier.padding(vertical = 4.dp)) }
                if (item.tasks.isEmpty()) {
                    Text("Rest day – nothing to do.", style = MaterialTheme.typography.bodySmall)
                }
                DayTaskList(item.tasks, item.editable, onToggle, onLogAmount)
                if (item.allDone && item.editable) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(PhosphorIcons.Regular.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(if (isToday) "All done for today" else "All done", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
