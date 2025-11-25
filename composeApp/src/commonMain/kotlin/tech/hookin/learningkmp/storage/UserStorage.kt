package tech.hookin.learningkmp.storage
import com.russhwolf.settings.Settings
import tech.hookin.learningkmp.objects.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString




expect fun provideSettings(): Settings

object UserStorage {
    private val settings: Settings by lazy { provideSettings() }
    private const val USERS_KEY = "users"

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
}
