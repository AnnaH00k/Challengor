package tech.hookin.learningkmp.pages


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import tech.hookin.learningkmp.objects.User
import tech.hookin.learningkmp.ui.components.H2
import tech.hookin.learningkmp.ui.components.MainButton
import tech.hookin.learningkmp.ui.components.MainPage
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.SignOut
import tech.hookin.learningkmp.objects.Challenge
import tech.hookin.learningkmp.objects.ChallengeType
import tech.hookin.learningkmp.objects.ChallengeTypeRef
import tech.hookin.learningkmp.ui.components.MainTextInput


@Composable
fun Dashboard(
    backClick: () -> Unit,
    currentUser: User?,
    onRequireLogin: () -> Unit,
    registeredUsers: List<User>,
    onLogout: () -> Unit,
    allChallenges: List<Challenge>,
    allChallengeTypes: List<ChallengeType>,
    allChallengeTypeRefs: List<ChallengeTypeRef>,
    onCreateChallengeWithType: (name: String, description: String, typeTitle: String) -> Unit
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
        return
    }

    val isAdmin = currentUser.email == "annahook@gmx.de" || currentUser.email == "annahook0@gmail.com"
    var showAdminBoard by remember { mutableStateOf(isAdmin) }

    if (isAdmin && showAdminBoard) {
        // Admin view
        AdminBoard(
            backClick = backClick,
            currentUser = currentUser,
            allChallenges = allChallenges,
            allChallengeTypeRefs = allChallengeTypeRefs,
            onRequireLogin = onRequireLogin,
            onLogout = onLogout,
            registeredUsers = registeredUsers,
            onSwitchToUserDashboard = { showAdminBoard = false }
        )
    } else {
        // Normal user dashboard (also visible for admin when toggled)
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

                    if (isAdmin) {
                        MainButton(
                            text = "Admin Board",
                            onClick = { showAdminBoard = true }
                        )
                    }
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

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 800.dp)
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var challengeName by remember { mutableStateOf("") }
                        var challengeDescription by remember { mutableStateOf("") }
                        var challengeType by remember { mutableStateOf("") }


                        H2("Create a new challenge")

                        MainTextInput(
                            value = challengeName,
                            onValueChange = { challengeName = it },
                            label = "Challenge name",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        MainTextInput(
                            value = challengeDescription,
                            onValueChange = { challengeDescription = it },
                            label = "Description",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false
                        )
                        MainTextInput(
                            value = challengeType,
                            onValueChange = { challengeType = it },
                            label = "Type(s) of Challenge (comma separated)",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false
                        )


                        MainButton(
                            text = "Save challenge",
                            enabled = challengeName.isNotBlank() && challengeDescription.isNotBlank() && challengeType.isNotBlank(),
                            onClick = {
                                onCreateChallengeWithType(
                                    challengeName.trim(),
                                    challengeDescription.trim(),
                                    challengeType.trim()
                                )
                                challengeName = ""
                                challengeDescription = ""
                                challengeType = ""
                            }
                        )

                        H2("Your challenges")

                        val myChallenges = allChallenges.filter { ch ->
                            currentUser.createdChallengeIds.contains(ch.id)
                        }

                        if (myChallenges.isEmpty()) {
                            Text("You have not created any challenges yet.")
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                myChallenges.forEach { ch ->
                                    Text("• ${ch.name}")
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}
