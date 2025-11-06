package tech.hookin.learningkmp.pages

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.hookin.learningkmp.objects.User
import tech.hookin.learningkmp.ui.components.H2
import tech.hookin.learningkmp.ui.components.HexToColor
import tech.hookin.learningkmp.ui.components.MainButton
import tech.hookin.learningkmp.ui.components.MainPage
import kotlin.time.ExperimentalTime

private val borderColor = HexToColor("#435A4D")
private val borderWidth = 1.dp

@Composable
private fun TableCell(
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(borderWidth, borderColor)
            .padding(8.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 40.dp),
    contentAlignment = Alignment.Center,) {
        Text(
            text = value,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun UserTableColumn(
    header: String,
    values: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(borderWidth, borderColor)
            .width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TableCell(header)
        values.forEach { TableCell(it) }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun VerticalTable(registeredUsers: List<User>) {
    val scrollState = rememberScrollState()

    val idValues = registeredUsers.map { it.id.toString() }
    val nameValues = registeredUsers.map { it.name }
    val emailValues = registeredUsers.map { it.email }
    val passwordValues = registeredUsers.map { it.password }
    val registeredOnValues = registeredUsers.map { it.registeredOn.toString() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.border(borderWidth, borderColor).wrapContentWidth()
        ) {
            UserTableColumn("ID", idValues)
            UserTableColumn("Name", nameValues)
            UserTableColumn("Email", emailValues)
            UserTableColumn("Password", passwordValues)
            UserTableColumn("Registered on", registeredOnValues)
        }
    }
}

@Composable
fun Dashboard(
    backClick: () -> Unit,
    currentUser: User,
    registeredUsers: List<User>
) {
    MainPage(
        background = "#779F7F",
        textColor = "#202226"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                MainButton(
                    text = "Back",
                    onClick = backClick,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                H2("Welcome ${currentUser.name}")

                Text(
                    " ${currentUser.email}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    "ID: ${currentUser.id}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                H2("All Registered Users")
                VerticalTable(registeredUsers)
            }
        }
    }
}
