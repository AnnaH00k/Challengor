package tech.hookin.learningkmp.storage
import com.russhwolf.settings.Settings
import tech.hookin.learningkmp.objects.User
expect fun provideSettings(): Settings

object UserStorage {
    private val settings: Settings by lazy { provideSettings() }
    private const val USERS_KEY = "users"
    private const val CURRENT_USER_KEY = "current_user"

    private val json = kotlinx.serialization.json.Json { encodeDefaults = true; ignoreUnknownKeys = true }

    fun saveUsers(users: List<User>) {
        val jsonString = json.encodeToString(users)
        settings.putString(USERS_KEY, jsonString)
    }

    fun loadUsers(): List<User> {
        val jsonString = settings.getStringOrNull(USERS_KEY) ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCurrentUser(user: User?) {
        if (user == null) {
            settings.remove(CURRENT_USER_KEY)
        } else {
            val jsonString = json.encodeToString(user)
            settings.putString(CURRENT_USER_KEY, jsonString)
        }
    }

    fun loadCurrentUser(): User? {
        val jsonString = settings.getStringOrNull(CURRENT_USER_KEY) ?: return null
        return try {
            json.decodeFromString<User>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

}
