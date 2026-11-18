package tech.hookin.challengor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import tech.hookin.challengor.domain.DayProgress
import tech.hookin.challengor.ui.utils.formatAmountShort
import tech.hookin.challengor.ui.utils.weekdayShort

/**
 * One cell per day of a participation: filled = all done, rust outline = behind plan on that day,
 * no fill = rest day. Days with only measurable tasks show the logged amount. Tapping selects a day.
 */
@Composable
fun DayStrip(
    days: List<DayProgress>,
    today: LocalDate,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = days.indexOfFirst { it.date == selected }.coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (selectedIndex - 2).coerceAtLeast(0))

    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(days, key = { it.date.toEpochDays() }) { day ->
            DayCell(day, isToday = day.date == today, isSelected = day.date == selected, today = today) {
                onSelect(day.date)
            }
        }
    }
}

@Composable
private fun DayCell(
    day: DayProgress,
    isToday: Boolean,
    isSelected: Boolean,
    today: LocalDate,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    // Short days that were made up (or covered by working ahead) don't count as missed.
    val missed = day.date < today && day.isBehind
    val container = when {
        day.isComplete -> colors.primary
        day.isRestDay -> Color.Transparent
        else -> colors.surface
    }
    val content = if (day.isComplete) colors.onPrimary else colors.onSurface
    val border = when {
        isSelected -> BorderStroke(2.dp, colors.onBackground)
        isToday -> BorderStroke(2.dp, colors.primary)
        missed -> BorderStroke(1.dp, colors.error)
        else -> BorderStroke(1.dp, colors.outlineVariant)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.width(52.dp),
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = content,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(day.date.weekdayShort(), style = MaterialTheme.typography.labelSmall)
            Text(day.date.day.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = when {
                    day.isRestDay -> "rest"
                    day.isMeasurableOnly -> formatAmountShort(day.amounts.values.sum())
                    else -> "${day.done}/${day.total}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (missed) colors.error else content,
            )
        }
    }
}
