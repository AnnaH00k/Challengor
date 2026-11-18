package tech.hookin.challengor.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ValidationTest {

    @Test
    fun validEmailHasNoError() {
        assertNull(validateEmail("anna@example.com"))
    }

    @Test
    fun emailErrors() {
        assertEquals("Email cannot contain whitespace", validateEmail("an na@example.com"))
        assertEquals("Email contains invalid characters", validateEmail("anna<@example.com"))
        assertEquals("Invalid email format", validateEmail("anna@example"))
    }

    @Test
    fun passwordRules() {
        assertNull(validatePassword("secret1"))
        assertNotNull(validatePassword("short"))
        assertNotNull(validatePassword("with space"))
        assertNotNull(validatePassword("bad{char}"))
    }

    @Test
    fun parseTopicsNormalizesAndDeduplicates() {
        assertEquals(listOf("sport", "music"), parseTopics(" Sport, music ,SPORT,, "))
        assertEquals(emptyList(), parseTopics(" , "))
    }
}
