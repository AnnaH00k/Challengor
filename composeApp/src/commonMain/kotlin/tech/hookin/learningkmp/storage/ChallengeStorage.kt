package tech.hookin.learningkmp.storage

import com.russhwolf.settings.Settings
import tech.hookin.learningkmp.objects.Challenge

object ChallengeStorage {
    private val settings: Settings by lazy { provideSettings() }
    private const val CHALLENGES_KEY = "challenges"
    private val json = kotlinx.serialization.json.Json { encodeDefaults = true; ignoreUnknownKeys = true }

    fun saveChallenges(challenges: List<Challenge>) {
        val jsonString = json.encodeToString(challenges)
        settings.putString(CHALLENGES_KEY, jsonString)
    }

    fun loadChallenges(): List<Challenge> {
        val jsonString = settings.getStringOrNull(CHALLENGES_KEY) ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }
}