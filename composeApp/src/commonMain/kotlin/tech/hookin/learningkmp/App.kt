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
import tech.hookin.learningkmp.pages.AdminBoard
import tech.hookin.learningkmp.pages.Login
import tech.hookin.learningkmp.pages.Register
import tech.hookin.learningkmp.pages.Dashboard
import tech.hookin.learningkmp.storage.UserStorage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import tech.hookin.learningkmp.objects.Challenge
import tech.hookin.learningkmp.objects.ChallengeType
import tech.hookin.learningkmp.objects.ChallengeTypeRef
import tech.hookin.learningkmp.storage.ChallengeStorage
import tech.hookin.learningkmp.storage.ChallengeTypeRefStorage
import tech.hookin.learningkmp.storage.ChallengeTypeStorage


enum class Screen {
    HOME,
    LOGIN,
    REGISTER,
    DASHBOARD,
    ADMINBOARD
}
private fun normalizeTypeName(raw: String): String =
    raw.trim().lowercase()


@OptIn(ExperimentalTime::class)
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var registeredUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var allChallenges by remember { mutableStateOf<List<Challenge>>(emptyList()) }
    var allChallengeTypes by remember { mutableStateOf<List<ChallengeType>>(emptyList()) }
    var allChallengeTypeRefs by remember { mutableStateOf<List<ChallengeTypeRef>>(emptyList()) }
    val now = Clock.System.now()
    val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())


    LaunchedEffect(Unit) {
        registeredUsers = UserStorage.loadUsers()
        currentUser = UserStorage.loadCurrentUser()
        allChallenges = ChallengeStorage.loadChallenges()
        allChallengeTypes = ChallengeTypeStorage.loadChallengeTypes()
        allChallengeTypeRefs = ChallengeTypeRefStorage.loadRefs()

    }
    LaunchedEffect(registeredUsers) {
        UserStorage.saveUsers(registeredUsers)
    }
    LaunchedEffect(currentUser) {
        UserStorage.saveCurrentUser(currentUser)
    }
    LaunchedEffect(allChallenges) {
        ChallengeStorage.saveChallenges(allChallenges)
    }
    LaunchedEffect(allChallengeTypes) {
        ChallengeTypeStorage.saveChallengeTypes(allChallengeTypes)
    }
    LaunchedEffect(allChallengeTypeRefs) {
        ChallengeTypeRefStorage.saveRefs(allChallengeTypeRefs)
    }


    when (currentScreen) {
        Screen.HOME -> {
            Landingpage(
                isLoggedIn = currentUser != null,
                NavigateToLogin = {
                    currentScreen = Screen.LOGIN
                },
                NavigateToRegister = {
                    currentScreen = Screen.REGISTER
                },
                NavigateToDashboard = {
                    currentScreen = Screen.DASHBOARD
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
                        createdChallengeIds = emptyList()
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
            Dashboard(
                backClick = { currentScreen = Screen.HOME },
                currentUser = currentUser,
                onRequireLogin = { currentScreen = Screen.HOME },
                registeredUsers = registeredUsers,
                onLogout = {
                    currentUser = null
                    currentScreen = Screen.HOME
                },
                allChallenges = allChallenges,
                allChallengeTypes = allChallengeTypes,
                allChallengeTypeRefs = allChallengeTypeRefs,
                onCreateChallengeWithType = { name, description, typeTitle ->
                    val user = currentUser ?: return@Dashboard
                    // 1) parse comma-separated type names
                    val rawNames = typeTitle.split(',')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    val normalizedNames = rawNames
                        .map { normalizeTypeName(it) }
                        .distinct()

                    if (rawNames.isEmpty()) return@Dashboard
                    if (normalizedNames.isEmpty()) return@Dashboard

                    // 2) ensure each type exists (case-insensitive via normalized name)
                    var localTypes = allChallengeTypes
                    val ensuredTypes = mutableListOf<ChallengeType>()
                    normalizedNames.forEach { norm ->
                        val existing = localTypes.find { it.name == norm }
                        val type = existing ?: ChallengeType(name = norm).also { newType ->
                            localTypes = localTypes + newType
                        }
                        ensuredTypes += type
                    }
                    allChallengeTypes = localTypes


                    // 3) create challenge
                    val newChallengeId = (allChallenges.maxOfOrNull { it.id } ?: 0) + 1
                    val newChallenge = Challenge(
                        id = newChallengeId,
                        name = name,
                        description = description
                    )
                    allChallenges = allChallenges + newChallenge

                    // 4) create refs for each type
                    val newRefs = ensuredTypes.map { type ->
                        ChallengeTypeRef(
                            challengeId = newChallengeId,
                            typeName = type.name
                        )
                    }
                    allChallengeTypeRefs = allChallengeTypeRefs + newRefs

                    // 5) update user’s createdChallengeIds
                    val updatedUser = user.copy(
                        createdChallengeIds = user.createdChallengeIds + newChallengeId
                    )
                    registeredUsers = registeredUsers.map {
                        if (it.id == updatedUser.id) updatedUser else it
                    }
                    currentUser = updatedUser
                }
            )
        }

        Screen.ADMINBOARD -> {
            AdminBoard(
                backClick = { currentScreen = Screen.HOME },
                currentUser = currentUser,
                registeredUsers = registeredUsers,
                allChallenges = allChallenges,
                allChallengeTypeRefs = allChallengeTypeRefs,
                onRequireLogin = {
                    currentScreen = Screen.HOME
                },
                onSwitchToUserDashboard = {
                    currentScreen = Screen.DASHBOARD
                },
                onLogout = {
                    currentUser = null
                    currentScreen = Screen.HOME
                }
            )
        }
    }

}