package tech.hookin.challengor.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tech.hookin.challengor.model.Role
import tech.hookin.challengor.model.UserProfile
import tech.hookin.challengor.model.newId

/** Local accounts and the logged-in user. Replaced by server login once the backend exists. */
class AuthRepository(
    private val store: LocalStore,
    private val passwordHasher: PasswordHasher,
    private val clock: AppClock,
) {
    val currentUser: Flow<UserProfile?> = store.state
        .map { it.currentProfile() }
        .distinctUntilChanged()

    fun currentUser(): UserProfile? = store.current.currentProfile()

    fun isNameTaken(name: String): Boolean =
        store.current.accounts.any { it.name.equals(name.trim(), ignoreCase = true) }

    fun isEmailTaken(email: String): Boolean =
        store.current.accounts.any { it.email.equals(email.trim(), ignoreCase = true) }

    /** Creates an account and logs it in. Returns `null` if the name or email is already taken. */
    fun register(name: String, email: String, password: String): UserProfile? {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        if (trimmedName.isEmpty() || isNameTaken(trimmedName) || isEmailTaken(trimmedEmail)) return null

        val salt = passwordHasher.newSalt()
        val account = LocalAccount(
            id = newId(),
            name = trimmedName,
            email = trimmedEmail,
            createdAt = clock.nowMillis(),
            role = roleFor(trimmedEmail),
            passwordHash = passwordHasher.hash(password, salt),
            salt = salt,
        )
        store.update { it.copy(accounts = it.accounts + account, currentUserId = account.id) }
        return account.toProfile(includeEmail = true)
    }

    /** Returns `null` if the email is unknown or the password is wrong. */
    fun login(email: String, password: String): UserProfile? {
        val account = store.current.accounts
            .find { it.email.equals(email.trim(), ignoreCase = true) }
            ?.takeIf { passwordHasher.verify(password, it.salt, it.passwordHash) }
            ?: return null
        store.update { it.copy(currentUserId = account.id) }
        return account.toProfile(includeEmail = true)
    }

    fun logout() = store.update { it.copy(currentUserId = null) }

    private fun LocalState.currentProfile(): UserProfile? =
        accounts.find { it.id == currentUserId }?.toProfile(includeEmail = true)

    companion object {
        private val ADMIN_EMAILS = setOf(
            "annahook@gmx.de",
            "annahook0@gmail.com",
        )

        fun roleFor(email: String): Role =
            if (email.trim().lowercase() in ADMIN_EMAILS) Role.ADMIN else Role.USER
    }
}
