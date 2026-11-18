package tech.hookin.challengor.ui.screens.create

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import tech.hookin.challengor.data.AppContainer
import tech.hookin.challengor.data.challenge
import tech.hookin.challengor.data.participantCount
import tech.hookin.challengor.data.tasksOf
import tech.hookin.challengor.domain.ChallengeDraft
import tech.hookin.challengor.domain.TaskDraft
import tech.hookin.challengor.domain.planDays
import tech.hookin.challengor.domain.validate
import tech.hookin.challengor.model.ScheduleMode
import tech.hookin.challengor.model.Timing
import tech.hookin.challengor.model.Visibility

enum class TimingKind { RELATIVE, FIXED }

data class CreateChallengeUiState(
    val draft: ChallengeDraft,
    val timingKind: TimingKind = TimingKind.RELATIVE,
    /** Kept as text so the field can be empty while typing. */
    val durationText: String = DEFAULT_DURATION.toString(),
    val fixedStart: LocalDate,
    val fixedEnd: LocalDate,
    /** Day of the plan being edited (day-plan mode). */
    val selectedDay: Int = 0,
    /** Shown after a failed save attempt. */
    val error: String? = null,
    /** Set when an existing challenge is edited instead of a new one created. */
    val editingId: String? = null,
    /** People taking part in the edited challenge; changes affect their progress. */
    val participantCount: Int = 0,
) {
    val isEditing: Boolean get() = editingId != null

    /** Number of days to edit, or 0 while the timing is invalid. */
    val planDays: Int get() = if (draft.timing.validate() == null) draft.timing.planDays else 0

    /** Key in [ChallengeDraft.tasks] the task editor currently works on. */
    val taskKey: Int? get() = if (draft.scheduleMode == ScheduleMode.DAILY_REPEAT) null else selectedDay

    val currentTasks: List<TaskDraft> get() = draft.tasks[taskKey].orEmpty()

    fun dayHasTasks(day: Int): Boolean = draft.tasks[day].orEmpty().any { it.title.isNotBlank() }
}

private const val DEFAULT_DURATION = 30
private const val DEFAULT_FIXED_DAYS = 7
private const val MAX_TARGET_DIGITS = 6

/** Creates a challenge, or edits the one with [editChallengeId] (creator only). */
class CreateChallengeViewModel(
    private val container: AppContainer,
    private val userId: String,
    editChallengeId: String? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState(editChallengeId))
    val uiState: StateFlow<CreateChallengeUiState> = _uiState.asStateFlow()

    fun setTitle(value: String) = updateDraft { copy(title = value) }
    fun setDescription(value: String) = updateDraft { copy(description = value) }
    fun setTopics(value: String) = updateDraft { copy(topicsInput = value) }
    fun setVisibility(value: Visibility) = updateDraft { copy(visibility = value) }

    fun setScheduleMode(mode: ScheduleMode) = _uiState.update { state ->
        val key = if (mode == ScheduleMode.DAILY_REPEAT) null else 0
        val tasks = state.draft.tasks.let { if (it[key].isNullOrEmpty()) it + (key to listOf(TaskDraft())) else it }
        state.copy(draft = state.draft.copy(scheduleMode = mode, tasks = tasks), selectedDay = 0, error = null)
    }

    fun setTimingKind(kind: TimingKind) = _uiState.update { it.copy(timingKind = kind).withTiming() }

    fun setDuration(text: String) {
        if (text.all(Char::isDigit) && text.length <= 3) _uiState.update { it.copy(durationText = text).withTiming() }
    }

    fun setFixedStart(date: LocalDate) = _uiState.update { state ->
        state.copy(fixedStart = date, fixedEnd = maxOf(date, state.fixedEnd)).withTiming()
    }

    fun setFixedEnd(date: LocalDate) = _uiState.update { it.copy(fixedEnd = date).withTiming() }

    fun selectDay(day: Int) = _uiState.update { it.copy(selectedDay = day) }

    fun addTask() = editTasks { it + TaskDraft() }
    fun removeTask(index: Int) = editTasks { tasks -> tasks.filterIndexed { i, _ -> i != index } }

    fun setTaskTitle(index: Int, title: String) = editTask(index) { copy(title = title) }

    /** A measured task has a daily amount target (e.g. 1000 words) instead of a checkbox. */
    fun setTaskMeasured(index: Int, measured: Boolean) = editTask(index) { copy(measured = measured) }

    fun setTaskTarget(index: Int, target: String) {
        if (target.all(Char::isDigit) && target.length <= MAX_TARGET_DIGITS) editTask(index) { copy(target = target) }
    }

    fun setTaskUnit(index: Int, unit: String) = editTask(index) { copy(unit = unit) }

    /** Day plan: replaces the selected day's tasks with copies of the ones of the day before. */
    fun copyPreviousDay() = _uiState.update { state ->
        val previous = state.draft.tasks[state.selectedDay - 1]?.map { it.copy(id = null) } ?: return@update state
        state.copy(draft = state.draft.copy(tasks = state.draft.tasks + (state.selectedDay to previous)))
    }

    /** Saves the challenge and returns its id, or `null` (with [CreateChallengeUiState.error] set) if invalid. */
    fun save(): String? {
        val state = _uiState.value
        state.draft.validate()?.let { message ->
            _uiState.update { it.copy(error = message) }
            return null
        }
        val saved = state.editingId
            ?.let { container.challenges.update(it, state.draft, userId) }
            ?: if (state.isEditing) null else container.challenges.create(state.draft, userId)
        if (saved == null) _uiState.update { it.copy(error = "Only the creator can edit this challenge") }
        return saved?.id
    }

    private fun initialState(editChallengeId: String?): CreateChallengeUiState {
        val today = container.clock.today()
        val new = CreateChallengeUiState(
            draft = ChallengeDraft(tasks = mapOf(null to listOf(TaskDraft()))),
            fixedStart = today,
            fixedEnd = today.plus(DEFAULT_FIXED_DAYS - 1, DateTimeUnit.DAY),
        )
        val state = container.store.current
        val challenge = editChallengeId?.let(state::challenge)?.takeIf { it.creatorId == userId } ?: return new
        val timing = challenge.timing
        return new.copy(
            draft = ChallengeDraft.from(challenge, state.tasksOf(challenge.id)),
            timingKind = if (timing is Timing.Fixed) TimingKind.FIXED else TimingKind.RELATIVE,
            durationText = ((timing as? Timing.Relative)?.durationDays ?: DEFAULT_DURATION).toString(),
            fixedStart = (timing as? Timing.Fixed)?.startDate ?: new.fixedStart,
            fixedEnd = (timing as? Timing.Fixed)?.endDate ?: new.fixedEnd,
            editingId = challenge.id,
            participantCount = state.participantCount(challenge.id),
        )
    }

    private fun updateDraft(change: ChallengeDraft.() -> ChallengeDraft) =
        _uiState.update { it.copy(draft = it.draft.change(), error = null) }

    private fun editTask(index: Int, change: TaskDraft.() -> TaskDraft) =
        editTasks { tasks -> tasks.mapIndexed { i, task -> if (i == index) task.change() else task } }

    private fun editTasks(change: (List<TaskDraft>) -> List<TaskDraft>) = _uiState.update { state ->
        val key = state.taskKey
        state.copy(draft = state.draft.copy(tasks = state.draft.tasks + (key to change(state.currentTasks))), error = null)
    }

    private fun CreateChallengeUiState.withTiming(): CreateChallengeUiState {
        val timing = when (timingKind) {
            TimingKind.RELATIVE -> Timing.Relative(durationText.toIntOrNull() ?: 0)
            TimingKind.FIXED -> Timing.Fixed(fixedStart, fixedEnd)
        }
        val updated = copy(draft = draft.copy(timing = timing), error = null)
        return updated.copy(selectedDay = selectedDay.coerceIn(0, (updated.planDays - 1).coerceAtLeast(0)))
    }
}
