package tech.hookin.challengor.data

import com.russhwolf.settings.MapSettings
import tech.hookin.challengor.model.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryTest {
    private val settings = MapSettings()
    private val auth get() = testContainer(settings).auth

    @Test
    fun registerLogsInAndStoresOnlyAHash() {
        val repo = auth
        val user = assertNotNull(repo.register("Anna", "anna@example.com", "secret1"))
        assertEquals(user, repo.currentUser())
        assertEquals(Role.USER, user.role)
        assertTrue("secret1" !in settings.getString("state_v2", ""))
    }

    @Test
    fun duplicateNameOrEmailIsRejected() {
        val repo = auth
        repo.register("Anna", "anna@example.com", "secret1")
        assertNull(repo.register("anna", "other@example.com", "secret1"))
        assertNull(repo.register("Other", "ANNA@example.com", "secret1"))
    }

    @Test
    fun loginChecksPasswordAndSurvivesRestart() {
        auth.register("Anna", "anna@example.com", "secret1")
        auth.logout()
        assertNull(auth.currentUser())
        assertNull(auth.login("anna@example.com", "wrong!!"))
        assertNotNull(auth.login("Anna@Example.com", "secret1"))
        assertEquals("Anna", auth.currentUser()?.name, "a new repository instance reads the saved session")
    }

    @Test
    fun adminRoleComesFromTheEmail() {
        assertEquals(Role.ADMIN, AuthRepository.roleFor(" AnnaHook@gmx.de "))
        assertEquals(Role.USER, AuthRepository.roleFor("someone@example.com"))
    }

    @Test
    fun legacyDataIsMigratedOnce() {
        settings.putString(
            "users",
            """[{"id":1,"name":"Old","password":"secret1","email":"old@example.com","registeredOn":"2025-05-01T10:00","createdChallengeIds":[7]}]""",
        )
        settings.putString("challenges", """[{"id":7,"name":"Run","description":"5 km","tasks":[]}]""")
        settings.putString("challenge_type_refs", """[{"challengeId":7,"typeName":"sport"}]""")
        settings.putString("current_user", """{"id":1,"name":"Old","email":"old@example.com","registeredOn":"x"}""")

        val container = testContainer(settings)
        assertEquals("Old", container.auth.currentUser()?.name)
        val challenge = container.store.current.challenges.single()
        assertEquals("Run", challenge.title)
        assertEquals(listOf("sport"), challenge.topics)
        assertEquals(container.auth.currentUser()?.id, challenge.creatorId)
        assertTrue("secret1" !in settings.getString("state_v2", ""), "plain-text password is hashed")

        container.auth.logout()
        assertNotNull(testContainer(settings).auth.login("old@example.com", "secret1"))
    }
}
