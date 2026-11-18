package tech.hookin.challengor.ui.navigation

import kotlinx.serialization.Serializable

// Typed destinations of the app's navigation graph.

@Serializable
data object LandingRoute

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

/** The dashboard with its sections (Today, Explore, …). */
@Serializable
data object DashboardRoute

@Serializable
data object CreateChallengeRoute

@Serializable
data object AdminRoute

@Serializable
data class ChallengeRoute(val id: String)

@Serializable
data class UserRoute(val id: String)

@Serializable
data class EditChallengeRoute(val id: String)
