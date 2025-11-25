package tech.hookin.learningkmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import tech.hookin.learningkmp.components.AutoIncrementIdGenerator
import tech.hookin.learningkmp.objects.User
import tech.hookin.learningkmp.pages.Login
import tech.hookin.learningkmp.pages.Register
import tech.hookin.learningkmp.pages.Dashboard
import tech.hookin.learningkmp.storage.UserStorage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class Screen {
    HOME,
    LOGIN,
    REGISTER,
    DASHBOARD
}

@OptIn(ExperimentalTime::class)
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var registeredUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    val now = Clock.System.now()
    val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())

    LaunchedEffect(Unit) {
        registeredUsers = UserStorage.loadUsers()
    }

    LaunchedEffect(registeredUsers) {
        UserStorage.saveUsers(registeredUsers)
    }

    when (currentScreen) {
        Screen.HOME -> {
            Landingpage(
                NavigateToLogin = {
                    currentScreen = Screen.LOGIN
                },
                NavigateToRegister = {
                    currentScreen = Screen.REGISTER
                }
            )
        }

        Screen.LOGIN -> {
            Login(
                BackClick = {
                    currentScreen = Screen.HOME
                },
                onLoginSubmit = { email, password ->
                    val user = registeredUsers.find { it.email == email && it.password == password }
                    if (user != null) {
                        currentUser = user
                        currentScreen = Screen.DASHBOARD
                        true
                    } else {
                        false
                    }
                }
            )
        }

        Screen.REGISTER -> {
            Register(
                BackClick = {
                    currentScreen = Screen.HOME
                },
                onRegisterSubmit = { name, email, password ->
                    val newUser = User(
                        id = AutoIncrementIdGenerator(registeredUsers) { user -> user.id },
                        name = name,
                        email = email,
                        password = password,
                        registeredOn = localDateTime.toString(),
                        createdChallenges = emptyList()
                    )
                    registeredUsers = registeredUsers + newUser
                    currentUser = newUser
                    currentScreen = Screen.DASHBOARD
                },
                registeredUsers = registeredUsers,
                onGoToLogin = {
                    currentScreen = Screen.LOGIN
                }
            )
        }


        Screen.DASHBOARD -> {
            currentUser?.let { user ->
                Dashboard(
                    backClick = { currentScreen = Screen.HOME },
                    currentUser = user,
                    registeredUsers = registeredUsers
                )
            }
        }

    }

}