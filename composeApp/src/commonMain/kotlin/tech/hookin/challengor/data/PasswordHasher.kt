package tech.hookin.challengor.data

import org.kotlincrypto.hash.sha2.SHA256
import kotlin.random.Random

/** Salted SHA-256 password hashing for locally stored accounts. */
class PasswordHasher(
    private val random: Random = Random.Default,
) {
    fun newSalt(): String = random.nextBytes(SALT_BYTES).toHex()

    fun hash(password: String, salt: String): String =
        SHA256().digest((salt + password).encodeToByteArray()).toHex()

    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        hash(password, salt) == expectedHash

    private companion object {
        const val SALT_BYTES = 16
    }
}

private fun ByteArray.toHex(): String =
    joinToString(separator = "") { byte -> (byte.toInt() and 0xFF).toString(16).padStart(2, '0') }
