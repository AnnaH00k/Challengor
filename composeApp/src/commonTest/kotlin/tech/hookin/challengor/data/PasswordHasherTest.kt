package tech.hookin.challengor.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {
    private val hasher = PasswordHasher()

    @Test
    fun hashIsDeterministicForSameSalt() {
        assertEquals(hasher.hash("secret1", "salt"), hasher.hash("secret1", "salt"))
    }

    @Test
    fun differentSaltGivesDifferentHash() {
        assertNotEquals(hasher.hash("secret1", "a"), hasher.hash("secret1", "b"))
    }

    @Test
    fun knownSha256Value() {
        // sha256("abc")
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            hasher.hash("abc", ""),
        )
    }

    @Test
    fun verifyAcceptsOnlyTheRightPassword() {
        val salt = hasher.newSalt()
        val hash = hasher.hash("secret1", salt)
        assertTrue(hasher.verify("secret1", salt, hash))
        assertFalse(hasher.verify("secret2", salt, hash))
    }

    @Test
    fun saltsAreRandomHex() {
        val salt = hasher.newSalt()
        assertEquals(32, salt.length)
        assertNotEquals(salt, hasher.newSalt())
    }
}
