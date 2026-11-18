package tech.hookin.challengor.ui.screens.challenge

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.ShareNetwork
import com.adamglin.phosphoricons.regular.Trash
import kotlinx.coroutines.launch
import tech.hookin.challengor.model.ScheduleMode
import tech.hookin.challengor.model.TaskTemplate
import tech.hookin.challengor.ui.components.Avatar
import tech.hookin.challengor.ui.components.BackTopBar
import tech.hookin.challengor.ui.components.ContentList
import tech.hookin.challengor.ui.components.DayStrip
import tech.hookin.challengor.ui.components.EmptyState
import tech.hookin.challengor.ui.components.ProgressRing
import tech.hookin.challengor.ui.components.SectionTitle
import tech.hookin.challengor.ui.components.StatusBadge
import tech.hookin.challengor.ui.components.GoalSummary
import tech.hookin.challengor.ui.components.TopicTag
import tech.hookin.challengor.ui.screens.today.DayChecklist
import tech.hookin.challengor.ui.screens.today.DayTaskList
import tech.hookin.challengor.ui.utils.challengeLink
import tech.hookin.challengor.ui.utils.describe
import tech.hookin.challengor.ui.utils.formatAmount
import tech.hookin.challengor.ui.utils.label
import tech.hookin.challengor.ui.utils.long
import tech.hookin.challengor.ui.utils.plural
import tech.hookin.challengor.ui.utils.rememberShareSheet
import tech.hookin.challengor.ui.utils.short
import tech.hookin.challengor.ui.theme.neumorphic
import tech.hookin.challengor.ui.theme.OnSage

private const val PREVIEW_DAYS = 7

@OptIn(ExperimentalLayoutApi::class)
@Suppress("DEPRECATION") // LocalClipboardManager: the new LocalClipboard needs platform-specific clip entries.
@Composable
fun ChallengeDetailScreen(
    viewModel: ChallengeDetailViewModel,
    onBack: () -> Unit,
    onOpenUser: (String) -> Unit,
    onEdit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val shareSheet = rememberShareSheet()
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }

    val challenge = state.challenge
    Scaffold(
        topBar = {
            BackTopBar(
                title = challenge?.title ?: "Challenge",
                onBack = onBack,
                actions = {
                    if (challenge != null) {
                        IconButton(onClick = {
                            val text = "Join my challenge \"${challenge.title}\" on Challengor: ${challengeLink(challenge.id)}"
                            if (shareSheet != null) {
                                shareSheet(text)
                            } else {
                                clipboard.setText(AnnotatedString(challengeLink(challenge.id)))
                                scope.launch { snackbar.showSnackbar("Link copied – send it to your friends") }
                            }
                        }) { Icon(PhosphorIcons.Regular.ShareNetwork, contentDescription = "Share") }
                    }
                    if (state.isCreator) {
                        IconButton(onClick = onEdit) {
                            Icon(PhosphorIcons.Regular.PencilSimple, contentDescription = "Edit challenge")
                        }
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(PhosphorIcons.Regular.Trash, contentDescription = "Delete challenge")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (challenge == null) {
            ContentList(Modifier.padding(padding)) { item { EmptyState("This challenge doesn't exist anymore.") } }
            return@Scaffold
        }

        ContentList(Modifier.padding(padding)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (challenge.description.isNotBlank()) Text(challenge.description)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        challenge.topics.forEach { TopicTag(it) }
                    }
                    InfoLine("Schedule", challenge.scheduleMode.label)
                    InfoLine("When", challenge.timing.describe())
                    InfoLine("Visibility", challenge.visibility.label)
                    state.creator?.let { creator ->
                        Row(
                            modifier = Modifier.clickable { onOpenUser(creator.id) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Avatar(creator.name, size = 28.dp)
                            Text("Created by ${creator.name}", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            val checklist = state.checklist
            if (checklist != null) {
                item { ProgressCard(checklist) }
                item {
                    DayStrip(
                        days = checklist.progress.days,
                        today = state.today,
                        selected = checklist.date,
                        onSelect = viewModel::selectDay,
                    )
                }
                item { DayChecklistCard(checklist, onToggle = viewModel::setDone, onLogAmount = viewModel::logAmount) }
                item {
                    OutlinedButton(onClick = { confirmLeave = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Leave challenge")
                    }
                }
            } else {
                item {
                    if (state.canJoin) {
                        Button(
                            onClick = viewModel::join,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) { Text("Join challenge") }
                    } else {
                        Text("This challenge has ended – it can't be joined anymore.", color = MaterialTheme.colorScheme.error)
                    }
                }
                item { TaskPreview(state) }
            }

            item { SectionTitle("Participants (${state.participants.size})") }
            if (state.participants.isEmpty()) {
                item { Text("Nobody takes part yet. Be the first!") }
            }
            state.participants.forEach { person ->
                item(key = "participant-${person.id}") {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenUser(person.id) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Avatar(person.name, size = 32.dp)
                        Text(person.name)
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "Delete challenge?",
            text = "It disappears for everyone, including participants' progress.",
            confirmLabel = "Delete",
            onConfirm = { confirmDelete = false; if (viewModel.delete()) onBack() },
            onDismiss = { confirmDelete = false },
        )
    }
    if (confirmLeave) {
        ConfirmDialog(
            title = "Leave challenge?",
            text = "Your progress is kept as \"left\". You can join again later and start fresh.",
            confirmLabel = "Leave",
            onConfirm = { confirmLeave = false; viewModel.leave() },
            onDismiss = { confirmLeave = false },
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$label:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ProgressCard(checklist: DayChecklist) {
    val progress = checklist.progress
    OnSage {
        Card(
            modifier = Modifier.fillMaxWidth().neumorphic(CardDefaults.shape),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    ProgressRing(progress, size = 96.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        StatusBadge(progress.status)
                        if (progress.goals.isEmpty()) {
                            Text("${progress.done} of ${progress.total} tasks done", fontWeight = FontWeight.SemiBold)
                        }
                        Text("On track: ${progress.onTrackPercent}% of what was due so far", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Your window: ${checklist.participation.startDate.short()} – ${checklist.participation.endDate.long()}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                // Amount goals: short days can be made up later, so show the running total vs. plan.
                progress.goals.forEach { GoalSummary(it) }
            }
        }
    }
}

@Composable
private fun DayChecklistCard(
    checklist: DayChecklist,
    onToggle: (taskId: String, done: Boolean) -> Unit,
    onLogAmount: (taskId: String, amount: Int) -> Unit,
) {
    OnSage {
        Card(
            modifier = Modifier.fillMaxWidth().neumorphic(CardDefaults.shape),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Day ${checklist.dayNumber} · ${checklist.date.long()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!checklist.editable) {
                    Text(
                        text = "You can tick tasks of today and earlier days while the challenge runs.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (checklist.tasks.isEmpty()) Text("Rest day – nothing to do.", modifier = Modifier.padding(top = 8.dp))
                DayTaskList(checklist.tasks, checklist.editable, onToggle, onLogAmount)
            }
        }
    }
}

/** Shows what participants will do, for people who haven't joined yet. */
@Composable
private fun TaskPreview(state: ChallengeDetailUiState) {
    val challenge = state.challenge ?: return
    OnSage {
        Card(
            modifier = Modifier.fillMaxWidth().neumorphic(CardDefaults.shape),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                when (challenge.scheduleMode) {
                    ScheduleMode.DAILY_REPEAT -> {
                        Text("Every day", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        state.tasksByDay[null].orEmpty().forEach { Text("•  " + it.previewLabel()) }
                    }

                    ScheduleMode.DAY_PLAN -> {
                        val days = state.tasksByDay.keys.filterNotNull().sorted()
                        days.take(PREVIEW_DAYS).forEach { day ->
                            Text("Day ${day + 1}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            state.tasksByDay[day].orEmpty().forEach { Text("•  " + it.previewLabel()) }
                        }
                        if (days.size > PREVIEW_DAYS) {
                            Text("+ ${plural(days.size - PREVIEW_DAYS, "more day")} with tasks", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

/** "Write (1,000 words a day)" for measurable tasks, otherwise just the title. */
private fun TaskTemplate.previewLabel(): String =
    target?.let { "$title (${formatAmount(it)} ${unit.orEmpty()} a day)" } ?: title

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel, color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
    )
}
