package tech.hookin.challengor.ui.screens.admin

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.hookin.challengor.data.AppContainer
import tech.hookin.challengor.data.progressOf
import tech.hookin.challengor.ui.components.BackTopBar
import tech.hookin.challengor.ui.components.ContentList
import tech.hookin.challengor.ui.components.SectionTitle
import tech.hookin.challengor.ui.uiStateOf
import tech.hookin.challengor.ui.utils.formatDate

data class AdminUserRow(
    val name: String,
    val email: String,
    val role: String,
    val registeredOn: String,
    val created: Int,
    val joined: Int,
    val completed: Int,
)

data class AdminUiState(
    val users: List<AdminUserRow> = emptyList(),
    val challengeCount: Int = 0,
    val participationCount: Int = 0,
    val tickedTasks: Int = 0,
)

class AdminViewModel(container: AppContainer) : ViewModel() {
    val uiState = uiStateOf(container.store) { state ->
        val today = container.clock.today()
        AdminUiState(
            users = state.accounts.sortedBy { it.createdAt }.map { account ->
                val participations = state.participations.filter { it.userId == account.id }
                AdminUserRow(
                    name = account.name,
                    email = account.email,
                    role = account.role.name.lowercase(),
                    registeredOn = formatDate(account.createdAt),
                    created = state.challenges.count { it.creatorId == account.id && !it.deleted },
                    joined = participations.count { !it.left },
                    completed = participations.count { state.progressOf(it, today)?.isSuccess == true },
                )
            },
            challengeCount = state.challenges.count { !it.deleted },
            participationCount = state.participations.count { !it.left },
            tickedTasks = state.completions.count { it.done },
        )
    }
}

@Composable
fun AdminBoardScreen(viewModel: AdminViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BackTopBar(title = "Admin board", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        ContentList(Modifier.padding(padding)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Total("Users", state.users.size)
                    Total("Challenges", state.challengeCount)
                    Total("Participations", state.participationCount)
                    Total("Ticked tasks", state.tickedTasks)
                }
            }
            item { SectionTitle("All registered users") }
            item { UsersTable(state.users) }
        }
    }
}

@Composable
private fun Total(label: String, value: Int) {
    Column {
        Text(value.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun UsersTable(users: List<AdminUserRow>) {
    val columns = listOf(
        "Name" to users.map { it.name },
        "Email" to users.map { it.email },
        "Role" to users.map { it.role },
        "Registered" to users.map { it.registeredOn },
        "Created" to users.map { it.created.toString() },
        "Joined" to users.map { it.joined.toString() },
        "100 %" to users.map { it.completed.toString() },
    )

    Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Row(modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline)) {
            columns.forEach { (header, values) ->
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    TableCell(header, isHeader = true)
                    values.forEach { TableCell(it) }
                }
            }
        }
    }
}

@Composable
private fun TableCell(value: String, isHeader: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .defaultMinSize(minHeight = 40.dp)
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text = value, fontWeight = if (isHeader) FontWeight.Bold else null, maxLines = 1)
    }
}
