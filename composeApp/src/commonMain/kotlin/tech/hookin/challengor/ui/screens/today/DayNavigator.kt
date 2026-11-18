package tech.hookin.challengor.ui.screens.today

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretLeft
import com.adamglin.phosphoricons.regular.CaretRight
import tech.hookin.challengor.ui.utils.short
import tech.hookin.challengor.ui.utils.weekdayShort

/** Selected day with arrows to step one day, shown centered in the dashboard's top bar. */
@Composable
fun DayNavigator(state: TodayUiState, onShiftDays: (Int) -> Unit) {
    val date = state.selectedDate
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onShiftDays(-1) }, modifier = Modifier.size(36.dp)) {
            Icon(PhosphorIcons.Regular.CaretLeft, contentDescription = "Previous day")
        }
        Text(
            text = "${date.weekdayShort()}, ${date.short()}",
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        IconButton(onClick = { onShiftDays(1) }, modifier = Modifier.size(36.dp)) {
            Icon(PhosphorIcons.Regular.CaretRight, contentDescription = "Next day")
        }
    }
}
