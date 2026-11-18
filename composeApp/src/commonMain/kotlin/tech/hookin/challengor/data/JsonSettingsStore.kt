package tech.hookin.challengor.data

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

/** Stores serializable values as JSON strings in [Settings]. */
class JsonSettingsStore(
    @PublishedApi internal val settings: Settings,
) {
    @PublishedApi
    internal val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    /** Returns the stored value, or `null` if it is missing or cannot be decoded. */
    inline fun <reified T> load(key: String): T? {
        val raw = settings.getStringOrNull(key) ?: return null
        return runCatching { json.decodeFromString<T>(raw) }.getOrNull()
    }

    /** Stores [value]; `null` removes the key. */
    inline fun <reified T> save(key: String, value: T?) {
        if (value == null) {
            settings.remove(key)
        } else {
            settings.putString(key, json.encodeToString(value))
        }
    }
}
