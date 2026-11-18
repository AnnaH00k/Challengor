package tech.hookin.challengor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import tech.hookin.challengor.data.AppContainer
import tech.hookin.challengor.ui.LocalAppContainer
import tech.hookin.challengor.ui.navigation.AdminRoute
import tech.hookin.challengor.ui.navigation.ChallengeRoute
import tech.hookin.challengor.ui.navigation.CreateChallengeRoute
import tech.hookin.challengor.ui.navigation.DashboardRoute
import tech.hookin.challengor.ui.navigation.EditChallengeRoute
import tech.hookin.challengor.ui.navigation.LandingRoute
import tech.hookin.challengor.ui.navigation.LoginRoute
import tech.hookin.challengor.ui.navigation.RegisterRoute
import tech.hookin.challengor.ui.navigation.UserRoute
import tech.hookin.challengor.ui.screens.LandingScreen
import tech.hookin.challengor.ui.screens.LoginScreen
import tech.hookin.challengor.ui.screens.RegisterScreen
import tech.hookin.challengor.ui.screens.admin.AdminBoardScreen
import tech.hookin.challengor.ui.screens.admin.AdminViewModel
import tech.hookin.challengor.ui.screens.challenge.ChallengeDetailScreen
import tech.hookin.challengor.ui.screens.challenge.ChallengeDetailViewModel
import tech.hookin.challengor.ui.screens.create.CreateChallengeScreen
import tech.hookin.challengor.ui.screens.create.CreateChallengeViewModel
import tech.hookin.challengor.ui.screens.dashboard.DashboardScreen
import tech.hookin.challengor.ui.screens.profile.ProfileScreen
import tech.hookin.challengor.ui.screens.profile.ProfileViewModel
import tech.hookin.challengor.ui.components.BackTopBar
import tech.hookin.challengor.ui.theme.ChallengorTheme

/**
 * Root of the app on every platform.
 * [initialChallengeId] comes from a shared link (web) and opens that challenge after login.
 */
@Composable
fun App(initialChallengeId: String? = null) {
    val container = remember { AppContainer() }

    CompositionLocalProvider(LocalAppContainer provides container) {
        ChallengorTheme {
            val user by container.auth.currentUser.collectAsStateWithLifecycle(container.auth.currentUser())
            val navController = rememberNavController()
            var pendingChallengeId by remember { mutableStateOf(initialChallengeId) }
            var knownUserId by remember { mutableStateOf(user?.id) }

            // Logging in or out swaps the whole back stack.
            LaunchedEffect(user?.id) {
                if (user?.id != knownUserId) {
                    knownUserId = user?.id
                    navController.resetTo(if (user != null) DashboardRoute else LandingRoute)
                }
            }
            // A shared link opens its challenge as soon as someone is logged in.
            LaunchedEffect(user?.id, pendingChallengeId) {
                val challengeId = pendingChallengeId
                if (user != null && challengeId != null) {
                    pendingChallengeId = null
                    navController.navigate(ChallengeRoute(challengeId))
                }
            }

            NavHost(
                navController = navController,
                startDestination = if (user != null) DashboardRoute else LandingRoute,
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            ) {
                composable<LandingRoute> {
                    LandingScreen(
                        onLogin = { navController.navigate(LoginRoute) },
                        onRegister = { navController.navigate(RegisterRoute) },
                    )
                }
                composable<LoginRoute> {
                    LoginScreen(
                        onBack = navController::goBack,
                        onLogin = { email, password -> container.auth.login(email, password) != null },
                    )
                }
                composable<RegisterRoute> {
                    RegisterScreen(
                        onBack = navController::goBack,
                        isNameTaken = container.auth::isNameTaken,
                        isEmailTaken = container.auth::isEmailTaken,
                        onRegister = { name, email, password -> container.auth.register(name, email, password) },
                        onGoToLogin = { navController.navigate(LoginRoute) { popUpTo(LandingRoute) } },
                    )
                }

                composable<DashboardRoute> {
                    val me = user ?: return@composable
                    DashboardScreen(
                        user = me,
                        onOpenChallenge = { navController.navigate(ChallengeRoute(it)) },
                        onOpenUser = { navController.navigate(UserRoute(it)) },
                        onCreate = { navController.navigate(CreateChallengeRoute) },
                        onOpenAdmin = { navController.navigate(AdminRoute) },
                        onLogout = container.auth::logout,
                    )
                }
                composable<ChallengeRoute> { entry ->
                    val me = user ?: return@composable
                    val id = entry.toRoute<ChallengeRoute>().id
                    ChallengeDetailScreen(
                        viewModel = viewModel { ChallengeDetailViewModel(container, id, me.id) },
                        onBack = navController::goBack,
                        onOpenUser = { navController.navigate(UserRoute(it)) },
                        onEdit = { navController.navigate(EditChallengeRoute(id)) },
                    )
                }
                composable<EditChallengeRoute> { entry ->
                    val me = user ?: return@composable
                    val id = entry.toRoute<EditChallengeRoute>().id
                    CreateChallengeScreen(
                        viewModel = viewModel { CreateChallengeViewModel(container, me.id, editChallengeId = id) },
                        onBack = navController::goBack,
                        onCreated = { navController.goBack() },
                    )
                }
                composable<UserRoute> { entry ->
                    val me = user ?: return@composable
                    val id = entry.toRoute<UserRoute>().id
                    Scaffold(
                        topBar = { BackTopBar(title = "Profile", onBack = navController::goBack) },
                        containerColor = MaterialTheme.colorScheme.background,
                    ) { padding ->
                        Box(Modifier.padding(padding)) {
                            ProfileScreen(
                                viewModel = viewModel { ProfileViewModel(container, id, me.id) },
                                onOpenChallenge = { navController.navigate(ChallengeRoute(it)) },
                            )
                        }
                    }
                }
                composable<CreateChallengeRoute> {
                    val me = user ?: return@composable
                    CreateChallengeScreen(
                        viewModel = viewModel { CreateChallengeViewModel(container, me.id) },
                        onBack = navController::goBack,
                        onCreated = { id ->
                            navController.navigate(ChallengeRoute(id)) { popUpTo(DashboardRoute) }
                        },
                    )
                }
                composable<AdminRoute> {
                    val me = user ?: return@composable
                    if (!me.isAdmin) return@composable
                    AdminBoardScreen(
                        viewModel = viewModel { AdminViewModel(container) },
                        onBack = navController::goBack,
                    )
                }
            }
        }
    }
}

/**
 * Goes back one screen, but never pops the last one: a double tap on "back" would
 * otherwise leave an empty screen.
 */
private fun NavHostController.goBack() {
    if (previousBackStackEntry != null) popBackStack()
}

/** Clears the back stack and shows [route] as the only screen. */
private fun NavHostController.resetTo(route: Any) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
