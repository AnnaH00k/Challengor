package tech.hookin.challengor.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarCheck
import com.adamglin.phosphoricons.regular.Compass
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.ShieldCheck
import com.adamglin.phosphoricons.regular.Trophy
import com.adamglin.phosphoricons.regular.UserCircle
import com.adamglin.phosphoricons.regular.Users
import tech.hookin.challengor.model.UserProfile
import tech.hookin.challengor.ui.LocalAppContainer
import tech.hookin.challengor.ui.components.DashboardTopBar
import tech.hookin.challengor.ui.screens.explore.ExploreScreen
import tech.hookin.challengor.ui.screens.explore.ExploreViewModel
import tech.hookin.challengor.ui.screens.mychallenges.MyChallengesScreen
import tech.hookin.challengor.ui.screens.mychallenges.MyChallengesViewModel
import tech.hookin.challengor.ui.screens.people.PeopleScreen
import tech.hookin.challengor.ui.screens.people.PeopleViewModel
import tech.hookin.challengor.ui.screens.profile.ProfileScreen
import tech.hookin.challengor.ui.screens.profile.ProfileViewModel
import tech.hookin.challengor.ui.screens.today.TodayScreen
import tech.hookin.challengor.ui.screens.today.TodayViewModel
import tech.hookin.challengor.ui.utils.isWideScreen

enum class DashboardSection(val label: String, val shortLabel: String, val icon: ImageVector) {
    TODAY("Today", "Today", PhosphorIcons.Regular.CalendarCheck),
    EXPLORE("Explore", "Explore", PhosphorIcons.Regular.Compass),
    MINE("My challenges", "Mine", PhosphorIcons.Regular.Trophy),
    PEOPLE("People", "People", PhosphorIcons.Regular.Users),
    PROFILE("Profile", "Profile", PhosphorIcons.Regular.UserCircle),
}

/**
 * The logged-in home: every feature is one tap away. A bottom bar on phones,
 * a navigation rail on wide screens, and a "create" action everywhere.
 */
@Composable
fun DashboardScreen(
    user: UserProfile,
    onOpenChallenge: (String) -> Unit,
    onOpenUser: (String) -> Unit,
    onCreate: () -> Unit,
    onOpenAdmin: () -> Unit,
    onLogout: () -> Unit,
) {
    var section by rememberSaveable { mutableStateOf(DashboardSection.TODAY) }
    val wide = isWideScreen()
    val container = LocalAppContainer.current

    Scaffold(
        topBar = {
            DashboardTopBar(title = section.label) {
                if (user.isAdmin) {
                    IconButton(onClick = onOpenAdmin) { Icon(PhosphorIcons.Regular.ShieldCheck, contentDescription = "Admin board") }
                }
                IconButton(onClick = onCreate) { Icon(PhosphorIcons.Regular.Plus, contentDescription = "Create challenge") }
            }
        },
        bottomBar = {
            if (!wide) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    DashboardSection.entries.forEach { entry ->
                        NavigationBarItem(
                            selected = entry == section,
                            onClick = { section = entry },
                            icon = { Icon(entry.icon, contentDescription = entry.label) },
                            label = { Text(entry.shortLabel, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (section == DashboardSection.TODAY || section == DashboardSection.MINE) {
                ExtendedFloatingActionButton(
                    onClick = onCreate,
                    icon = { Icon(PhosphorIcons.Regular.Plus, contentDescription = null) },
                    text = { Text("New challenge") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Row(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (wide) {
                NavigationRail(containerColor = MaterialTheme.colorScheme.background) {
                    DashboardSection.entries.forEach { entry ->
                        NavigationRailItem(
                            selected = entry == section,
                            onClick = { section = entry },
                            icon = { Icon(entry.icon, contentDescription = entry.label) },
                            label = { Text(entry.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                val goTo = { target: DashboardSection -> section = target }
                when (section) {
                    DashboardSection.TODAY -> TodayScreen(
                        viewModel = viewModel { TodayViewModel(container, user.id) },
                        onOpenChallenge = onOpenChallenge,
                        onCreate = onCreate,
                        onExplore = { goTo(DashboardSection.EXPLORE) },
                    )

                    DashboardSection.EXPLORE -> ExploreScreen(
                        viewModel = viewModel { ExploreViewModel(container, user.id) },
                        onOpenChallenge = onOpenChallenge,
                        onCreate = onCreate,
                    )

                    DashboardSection.MINE -> MyChallengesScreen(
                        viewModel = viewModel { MyChallengesViewModel(container, user.id) },
                        onOpenChallenge = onOpenChallenge,
                        onCreate = onCreate,
                        onExplore = { goTo(DashboardSection.EXPLORE) },
                    )

                    DashboardSection.PEOPLE -> PeopleScreen(
                        viewModel = viewModel { PeopleViewModel(container, user.id) },
                        onOpenUser = onOpenUser,
                    )

                    DashboardSection.PROFILE -> ProfileScreen(
                        viewModel = viewModel(key = "profile-${user.id}") { ProfileViewModel(container, user.id, user.id) },
                        onOpenChallenge = onOpenChallenge,
                        onOpenAdmin = onOpenAdmin,
                        onLogout = onLogout,
                    )
                }
            }
        }
    }
}
