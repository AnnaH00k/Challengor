package tech.hookin.learningkmp.storage

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tech.hookin.learningkmp.objects.ChallengeType

object ChallengeTypeStorage {
    private val settings: Settings by lazy { provideSettings() }
    private const val CHALLENGE_TYPES_KEY = "challenge_types"

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun saveChallengeTypes(types: List<ChallengeType>) {
        val jsonString = json.encodeToString(types)
        settings.putString(CHALLENGE_TYPES_KEY, jsonString)
    }

    fun loadChallengeTypes(): List<ChallengeType> {
        val jsonString = settings.getStringOrNull(CHALLENGE_TYPES_KEY) ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
