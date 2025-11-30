package tech.hookin.learningkmp.pages

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.SignOut
import tech.hookin.learningkmp.objects.Challenge
import tech.hookin.learningkmp.objects.User
import tech.hookin.learningkmp.ui.components.H2
import tech.hookin.learningkmp.ui.components.HexToColor
import tech.hookin.learningkmp.ui.components.MainButton
import tech.hookin.learningkmp.ui.components.MainPage
import kotlin.time.ExperimentalTime
import tech.hookin.learningkmp.objects.ChallengeTypeRef


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
fun VerticalTable(
    registeredUsers: List<User>,
    allChallenges: List<Challenge>,
    allChallengeTypeRefs: List<ChallengeTypeRef>
) {
    val scrollState = rememberScrollState()

    val idValues = registeredUsers.map { it.id.toString() }
    val nameValues = registeredUsers.map { it.name }
    val emailValues = registeredUsers.map { it.email }
    val passwordValues = registeredUsers.map { it.password }
    val registeredOnValues = registeredUsers.map { it.registeredOn.toString() }
    val createdChallengesValues = registeredUsers.map { user ->
        val userChallenges = allChallenges.filter { it.id in user.createdChallengeIds }
        if (userChallenges.isEmpty()) {
            "No challenges created"
        } else {
            userChallenges.joinToString(", ") { it.name }
        }
    }
    val challengeTypesValues = registeredUsers.map { user ->
        val userChallengeIds = user.createdChallengeIds
        val userTypeNames = allChallengeTypeRefs
            .filter { it.challengeId in userChallengeIds }
            .map { it.typeName }
            .distinct()

        if (userTypeNames.isEmpty()) {
            "No challenge types"
        } else {
            userTypeNames.joinToString(", ")
        }
    }



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
            UserTableColumn("Created challenges", createdChallengesValues)
            UserTableColumn("ChallengeTypes", challengeTypesValues)

        }
    }
}

@Composable
fun AdminBoard(
    backClick: () -> Unit,
    currentUser: User?,
    registeredUsers: List<User>,
    allChallenges: List<Challenge>,
    allChallengeTypeRefs: List<ChallengeTypeRef>,
    onRequireLogin: () -> Unit,
    onLogout: () -> Unit,
    onSwitchToUserDashboard: () -> Unit

) {
    if (currentUser == null) {
        MainPage(background = "#BED8C3", textColor = "#202226") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Log in to see this page.")
                MainButton(
                    text = "Go to Landingpage",
                    onClick = onRequireLogin
                )
            }
        }
    }
    else {
        MainPage(
            background = "#BED8C3",
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = backClick) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.ArrowLeft,
                            contentDescription = "Back",
                            tint = Color(0xFF202226)
                        )
                    }
                    MainButton(
                        text = "User Board",
                        onClick = onSwitchToUserDashboard
                    )
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.SignOut,
                            contentDescription = "Logout",
                            tint = Color(0xFF202226)
                        )
                    }
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
                    VerticalTable(
                        registeredUsers = registeredUsers,
                        allChallenges = allChallenges,
                        allChallengeTypeRefs = allChallengeTypeRefs
                    )
                }
            }
        }
    }
}
