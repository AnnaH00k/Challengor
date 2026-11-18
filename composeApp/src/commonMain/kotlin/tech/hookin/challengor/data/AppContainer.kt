package tech.hookin.challengor.data

import com.russhwolf.settings.Settings

/** Builds the app's long-lived objects once. Hand-written instead of a DI library. */
class AppContainer(
    settings: Settings = provideSettings(),
    val clock: AppClock = SystemClock,
    passwordHasher: PasswordHasher = PasswordHasher(),
) {
    val store = LocalStore(settings, LegacyMigration(passwordHasher, clock, AuthRepository::roleFor))
    val auth = AuthRepository(store, passwordHasher, clock)
    val challenges = ChallengeRepository(store, clock)
    val preferences = AppPreferences(settings)
}
