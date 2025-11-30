package tech.hookin.learningkmp.storage

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tech.hookin.learningkmp.objects.ChallengeTypeRef

object ChallengeTypeRefStorage {
    private val settings: Settings by lazy { provideSettings() }
    private const val CHALLENGE_TYPE_REFS_KEY = "challenge_type_refs"

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun saveRefs(refs: List<ChallengeTypeRef>) {
        val jsonString = json.encodeToString(refs)
        settings.putString(CHALLENGE_TYPE_REFS_KEY, jsonString)
    }

    fun loadRefs(): List<ChallengeTypeRef> {
        val jsonString = settings.getStringOrNull(CHALLENGE_TYPE_REFS_KEY) ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
