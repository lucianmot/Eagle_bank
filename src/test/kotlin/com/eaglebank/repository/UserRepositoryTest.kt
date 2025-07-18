package com.eaglebank.repository

import com.eaglebank.config.TestJwtDecoderConfig
import com.eaglebank.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Import(TestJwtDecoderConfig::class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Ensures a clean DB per test
@Transactional
class UserRepositoryTest {
    @Autowired
    lateinit var userRepository: UserRepository

    // Factory for test users (ensures unique ID/email)
    private fun makeUser(
        id: String = "usr-" + UUID.randomUUID().toString(),
        email: String = "${UUID.randomUUID()}@example.com"
    ) = User(
        id = id,
        name = "Test User",
        addressLine1 = "100 Test Road",
        addressLine2 = null,
        addressLine3 = null,
        town = "Testville",
        county = "Testshire",
        postcode = "TE5T 1NG",
        phoneNumber = "+441234567890",
        email = email,
        passwordHash = "hashedpassword",
        createdTimestamp = Instant.now(),
        updatedTimestamp = Instant.now()
    )

    @Test
    fun `can create and fetch by id`() {
        val user = makeUser()
        userRepository.create(user)
        val found = userRepository.findById(user.id)
        assertNotNull(found)
        assertEquals(user.name, found?.name)
        assertEquals(user.email, found?.email)
    }

    @Test
    fun `findByEmail returns correct user`() {
        val user = makeUser()
        userRepository.create(user)
        val found = userRepository.findByEmail(user.email)
        assertNotNull(found)
        assertEquals(user.id, found?.id)
    }

    @Test
    fun `update changes user data`() {
        val user = makeUser()
        userRepository.create(user)
        val updated = user.copy(name = "Updated User", updatedTimestamp = Instant.now())
        userRepository.update(updated)
        val fromDb = userRepository.findById(user.id)
        assertEquals("Updated User", fromDb?.name)
    }

    @Test
    fun `delete removes the user`() {
        val user = makeUser()
        userRepository.create(user)
        userRepository.delete(user.id)
        val found = userRepository.findById(user.id)
        assertNull(found)
    }

    @Test
    fun `findById returns null for missing user`() {
        val found = userRepository.findById("usr-not-a-real-id")
        assertNull(found)
    }
}
