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

@Composable
fun Dashboard(
    backClick: () -> Unit,
    currentUser: User?,
    onRequireLogin: () -> Unit,
    registeredUsers: List<User>,
    onLogout: () -> Unit
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


                }
            }
        }
    }
}
