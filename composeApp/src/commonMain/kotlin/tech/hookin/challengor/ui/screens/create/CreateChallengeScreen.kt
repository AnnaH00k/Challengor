package tech.hookin.challengor.ui.screens.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.Copy
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.X
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import tech.hookin.challengor.domain.MAX_CHALLENGE_DAYS
import tech.hookin.challengor.domain.TaskDraft
import tech.hookin.challengor.model.ScheduleMode
import tech.hookin.challengor.model.Timing
import tech.hookin.challengor.model.Visibility
import tech.hookin.challengor.ui.components.BackTopBar
import tech.hookin.challengor.ui.components.ChoiceCard
import tech.hookin.challengor.ui.components.ContentList
import tech.hookin.challengor.ui.components.ErrorText
import tech.hookin.challengor.ui.components.SectionTitle
import tech.hookin.challengor.ui.components.TopicTag
import tech.hookin.challengor.ui.components.chipColors
import tech.hookin.challengor.ui.utils.formatAmount
import tech.hookin.challengor.ui.utils.fromEpochMillis
import tech.hookin.challengor.ui.utils.label
import tech.hookin.challengor.ui.utils.long
import tech.hookin.challengor.ui.utils.short
import tech.hookin.challengor.ui.utils.toEpochMillis

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateChallengeScreen(
    viewModel: CreateChallengeViewModel,
    onBack: () -> Unit,
    onCreated: (challengeId: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val draft = state.draft

    Scaffold(
        topBar = { BackTopBar(title = if (state.isEditing) "Edit challenge" else "New challenge", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        ContentList(Modifier.padding(padding)) {
            if (state.isEditing && state.participantCount > 0) {
                item {
                    Text(
                        (if (state.participantCount == 1) "1 person takes" else "${state.participantCount} people take") +
                            " part. Changes apply to their " +
                            "challenge too: entries of kept tasks stay, removed tasks no longer count.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            item { SectionTitle("1 · What is it about?") }
            item { FormField(draft.title, viewModel::setTitle, "Title") }
            item { FormField(draft.description, viewModel::setDescription, "Description (optional)", singleLine = false) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FormField(draft.topicsInput, viewModel::setTopics, "Topics, comma separated (e.g. sport, health)")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { draft.topics.forEach { TopicTag(it) } }
                }
            }

            item { SectionTitle("2 · Tasks per day") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceCard(
                        title = ScheduleMode.DAILY_REPEAT.label,
                        description = "One list of tasks that repeats every day, e.g. \"10 push-ups\".",
                        selected = draft.scheduleMode == ScheduleMode.DAILY_REPEAT,
                        onSelect = { viewModel.setScheduleMode(ScheduleMode.DAILY_REPEAT) },
                    )
                    ChoiceCard(
                        title = ScheduleMode.DAY_PLAN.label,
                        description = "A structured plan: different to-dos for each day.",
                        selected = draft.scheduleMode == ScheduleMode.DAY_PLAN,
                        onSelect = { viewModel.setScheduleMode(ScheduleMode.DAY_PLAN) },
                    )
                }
            }

            item { SectionTitle("3 · Time window") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceCard(
                        title = "Starts when someone joins",
                        description = "Everyone gets the same number of days from their own start.",
                        selected = state.timingKind == TimingKind.RELATIVE,
                        onSelect = { viewModel.setTimingKind(TimingKind.RELATIVE) },
                    )
                    ChoiceCard(
                        title = "Fixed dates",
                        description = "Everyone takes part in the same calendar window.",
                        selected = state.timingKind == TimingKind.FIXED,
                        onSelect = { viewModel.setTimingKind(TimingKind.FIXED) },
                    )
                    when (state.timingKind) {
                        TimingKind.RELATIVE -> FormField(
                            value = state.durationText,
                            onValueChange = viewModel::setDuration,
                            label = "Number of days (1–$MAX_CHALLENGE_DAYS)",
                            keyboardType = KeyboardType.Number,
                        )

                        TimingKind.FIXED -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DateButton("Start", state.fixedStart, onPick = viewModel::setFixedStart, Modifier.weight(1f))
                            DateButton("End", state.fixedEnd, onPick = viewModel::setFixedEnd, Modifier.weight(1f))
                        }
                    }
                }
            }

            item { SectionTitle("4 · Tasks") }
            if (draft.scheduleMode == ScheduleMode.DAY_PLAN) {
                item { DaySelector(state, viewModel::selectDay) }
            }
            item {
                TaskEditor(state = state, viewModel = viewModel)
            }

            item { SectionTitle("5 · Who can find it?") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceCard(
                        title = Visibility.PUBLIC.label,
                        description = "Listed in Explore and on your profile.",
                        selected = draft.visibility == Visibility.PUBLIC,
                        onSelect = { viewModel.setVisibility(Visibility.PUBLIC) },
                    )
                    ChoiceCard(
                        title = Visibility.LINK_ONLY.label,
                        description = "Only people you share the link with can join.",
                        selected = draft.visibility == Visibility.LINK_ONLY,
                        onSelect = { viewModel.setVisibility(Visibility.LINK_ONLY) },
                    )
                }
            }

            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    state.error?.let { ErrorText(it) }
                    Button(
                        onClick = { viewModel.save()?.let(onCreated) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) { Text(if (state.isEditing) "Save changes" else "Create challenge") }
                }
            }
        }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}

@Composable
private fun DaySelector(state: CreateChallengeUiState, onSelect: (Int) -> Unit) {
    val fixedStart = (state.draft.timing as? Timing.Fixed)?.startDate
    if (state.planDays == 0) {
        Text("Choose a valid time window first.", color = MaterialTheme.colorScheme.error)
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.planDays) { day ->
            val date = fixedStart?.plus(day, DateTimeUnit.DAY)
            FilterChip(
                selected = day == state.selectedDay,
                onClick = { onSelect(day) },
                label = { Text("Day ${day + 1}" + (date?.let { " · ${it.short()}" } ?: "") + if (state.dayHasTasks(day)) " ✓" else "") },
                colors = chipColors(),
            )
        }
    }
}

@Composable
private fun TaskEditor(
    state: CreateChallengeUiState,
    viewModel: CreateChallengeViewModel,
) {
    val dayPlan = state.draft.scheduleMode == ScheduleMode.DAY_PLAN
    if (dayPlan && state.planDays == 0) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (dayPlan) "To-dos of day ${state.selectedDay + 1}" else "Tasks for every day",
            style = MaterialTheme.typography.labelLarge,
        )
        state.currentTasks.forEachIndexed { index, task ->
            TaskDraftEditor(index, task, viewModel)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = viewModel::addTask) {
                Icon(PhosphorIcons.Regular.Plus, contentDescription = null)
                Text("Add task", modifier = Modifier.padding(start = 6.dp))
            }
            if (dayPlan && state.selectedDay > 0) {
                OutlinedButton(onClick = viewModel::copyPreviousDay) {
                    Icon(PhosphorIcons.Regular.Copy, contentDescription = null)
                    Text("Copy day ${state.selectedDay}", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
        // E.g. 1,000 words × 100 days: the goal participants work towards; short days can be made up.
        state.draft.goals.forEach { (unit, total) ->
            Text(
                text = "Total goal: ${formatAmount(total)} $unit in ${state.planDays} days",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (state.draft.goals.isNotEmpty()) {
            Text(
                "Participants log how much they did each day. Doing less one day can be made up later.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** Title, remove button and the optional daily amount target of one task. */
@Composable
private fun TaskDraftEditor(index: Int, task: TaskDraft, viewModel: CreateChallengeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = task.title,
                onValueChange = { viewModel.setTaskTitle(index, it) },
                label = { Text("Task ${index + 1}") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { viewModel.removeTask(index) }) {
                Icon(PhosphorIcons.Regular.X, contentDescription = "Remove task ${index + 1}")
            }
        }
        FilterChip(
            selected = task.measured,
            onClick = { viewModel.setTaskMeasured(index, !task.measured) },
            label = { Text("Measure an amount (e.g. words, km)") },
            colors = chipColors(),
        )
        if (task.measured) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = task.target,
                    onValueChange = { viewModel.setTaskTarget(index, it) },
                    label = { Text("Daily target") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = task.unit,
                    onValueChange = { viewModel.setTaskUnit(index, it) },
                    label = { Text("Unit") },
                    placeholder = { Text("words") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateButton(
    label: String,
    date: LocalDate,
    onPick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = modifier) {
        Icon(PhosphorIcons.Regular.CalendarBlank, contentDescription = null)
        Text("$label: ${date.long()}", modifier = Modifier.padding(start = 6.dp))
    }
    if (!open) return

    val pickerState = rememberDatePickerState(initialSelectedDateMillis = date.toEpochMillis())
    DatePickerDialog(
        onDismissRequest = { open = false },
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { onPick(LocalDate.fromEpochMillis(it)) }
                open = false
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } },
        colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        DatePicker(state = pickerState, colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface))
    }
}
