package tech.hookin.challengor.ui.screens.today

import androidx.compose.runtime.Composable
import tech.hookin.challengor.ui.components.AmountTaskRow
import tech.hookin.challengor.ui.components.TaskCheckRow

/** The tasks of one day: checkboxes for simple tasks, an amount field for measurable ones. */
@Composable
fun DayTaskList(
    tasks: List<DayTask>,
    editable: Boolean,
    onToggle: (taskId: String, done: Boolean) -> Unit,
    onLogAmount: (taskId: String, amount: Int) -> Unit,
) {
    tasks.forEach { dayTask ->
        val task = dayTask.task
        if (task.isMeasurable) {
            AmountTaskRow(
                title = task.title,
                amount = dayTask.amount,
                target = task.target!!,
                unit = task.unit.orEmpty(),
                enabled = editable,
                onLog = { onLogAmount(task.id, it) },
            )
        } else {
            TaskCheckRow(
                title = task.title,
                checked = dayTask.done,
                enabled = editable,
                onCheckedChange = { onToggle(task.id, it) },
            )
        }
    }
}
