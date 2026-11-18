package tech.hookin.challengor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CheckCircle
import tech.hookin.challengor.domain.GoalProgress
import tech.hookin.challengor.ui.utils.formatAmount
import kotlin.math.abs

private const val MAX_AMOUNT_DIGITS = 7

/**
 * A measurable task of one day: type in how much you did, e.g. 1240 words.
 * Saves when the keyboard's "done" is pressed or the field loses focus.
 */
@Composable
fun AmountTaskRow(
    title: String,
    amount: Int?,
    target: Int,
    unit: String,
    enabled: Boolean,
    onLog: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var text by remember(amount) { mutableStateOf(amount?.toString().orEmpty()) }
    var focused by remember { mutableStateOf(false) }
    fun commit() {
        val value = text.toIntOrNull() ?: 0
        if (value != (amount ?: 0)) onLog(value)
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title)
            Text("Daily target: ${formatAmount(target)} $unit", style = MaterialTheme.typography.labelSmall)
        }
        OutlinedTextField(
            value = text,
            onValueChange = { input -> if (input.all(Char::isDigit) && input.length <= MAX_AMOUNT_DIGITS) text = input },
            enabled = enabled,
            singleLine = true,
            placeholder = { Text("0") },
            suffix = { Text(unit, style = MaterialTheme.typography.labelSmall) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { commit(); focusManager.clearFocus() }),
            modifier = Modifier
                .width(150.dp)
                .onFocusChanged { state ->
                    if (focused && !state.isFocused) commit()
                    focused = state.isFocused
                },
        )
        if ((amount ?: 0) >= target) {
            Icon(PhosphorIcons.Regular.CheckCircle, contentDescription = "Daily target reached", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * Progress towards an amount goal, e.g. "45,300 / 100,000 words", and whether the
 * participant is ahead of plan or has something to make up.
 */
@Composable
fun GoalSummary(goal: GoalProgress, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${formatAmount(goal.achieved)} / ${formatAmount(goal.goal)} ${goal.unit}",
                fontWeight = FontWeight.SemiBold,
            )
            Text("${goal.percent}%", style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(
            progress = { goal.fraction },
            modifier = Modifier.fillMaxWidth(),
            color = colors.primary,
            trackColor = colors.outlineVariant,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
        val ahead = goal.aheadBy
        Text(
            text = when {
                goal.isReached -> "Goal reached!"
                ahead > 0 -> "${formatAmount(ahead)} ${goal.unit} ahead of plan"
                ahead < 0 -> "${formatAmount(abs(ahead))} ${goal.unit} to make up · planned by today: ${formatAmount(goal.dueGoal)}"
                else -> "Exactly on plan"
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (ahead < 0 && !goal.isReached) colors.error else colors.primary,
        )
    }
}
