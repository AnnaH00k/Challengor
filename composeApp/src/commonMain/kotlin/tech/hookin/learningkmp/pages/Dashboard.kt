package tech.hookin.learningkmp.pages

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.hookin.learningkmp.objects.User
import tech.hookin.learningkmp.ui.components.CenteredRow
import tech.hookin.learningkmp.ui.components.H2
import tech.hookin.learningkmp.ui.components.HexToColor
import tech.hookin.learningkmp.ui.components.MainButton
import tech.hookin.learningkmp.ui.components.MainPage

val borderColor = HexToColor("#435A4D")
private val borderWidth = 1.dp

@Composable
private fun TableCell(
    text: String,
    modifier: Modifier = Modifier,
    weight: Float = 1f,
    borderRight: Boolean = true
) {
    CenteredRow(modifier = Modifier .fillMaxWidth() .border(width = borderWidth, color = borderColor)) {
        Text(
            text = text,
            modifier = modifier
                .padding(8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
            )
    }

}

@Composable
fun VerticalTable(
    registeredUsers: List<User>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = borderWidth, color = borderColor),
    ) {
        Column(
            modifier = Modifier
                .weight(2f)
                .border(width = borderWidth, color = borderColor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TableCell("ID", weight = 1f)
            registeredUsers.forEach { user ->
                TableCell(user.id, weight = 1f)
            }
        }

        Column(
            modifier = Modifier
                .weight(2f)
                .border(width = borderWidth, color = borderColor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TableCell("Name", weight = 1f)
            registeredUsers.forEach { user ->
                TableCell(user.name, weight = 1f)
            }
        }

        Column(
            modifier = Modifier
                .weight(3f)
                .border(width = borderWidth, color = borderColor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TableCell("Email", weight = 1f)
            registeredUsers.forEach { user ->
                TableCell(user.email, weight = 1f)
            }
        }

        Column(
            modifier = Modifier
                .weight(3f)
                .border(width = borderWidth, color = borderColor),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TableCell("Password", weight = 1f)
            registeredUsers.forEach { user ->
                TableCell(user.password, weight = 1f)
            }
        }
    }
}

@Composable
fun Dashboard(
    BackClick: () -> Unit,
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
                    onClick = BackClick,
                )
            }


            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                H2("Welcome ${currentUser.name}")

                Text(
                    " ${currentUser.email}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    "ID: ${currentUser.id}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                H2("All Registered Users")

                VerticalTable(registeredUsers = registeredUsers)
            }
        }
    }
}
