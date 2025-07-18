package com.eaglebank.repository

import com.eaglebank.config.TestJwtDecoderConfig
import com.eaglebank.model.Account
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class AccountRepositoryTest {

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var userRepository: UserRepository

    // Simple helper to generate test accounts
    private fun makeAccount(
        accountNumber: String = "01${(1000000..9999999).random()}",
        userId: String,
        name: String = "Test Account",
        balance: Double = 1000.00
    ) = Account(
        accountNumber = accountNumber,
        userId = userId,
        name = name,
        accountType = "personal",
        balance = balance,
        currency = "GBP",
        sortCode = "10-10-10",
        createdTimestamp = Instant.now(),
        updatedTimestamp = Instant.now()
    )

    private fun createTestUser(userId: String = "usr-" + UUID.randomUUID()): String {
        val now = Instant.now()
        val user = com.eaglebank.model.User(
            id = userId,
            name = "Test User",
            addressLine1 = "123 Test St",
            addressLine2 = null,
            addressLine3 = null,
            town = "Testville",
            county = "Testshire",
            postcode = "T3ST 1NG",
            phoneNumber = "+441234567890",
            email = "${UUID.randomUUID()}@example.com",
            passwordHash = "hashedpassword",
            createdTimestamp = now,
            updatedTimestamp = now
        )
        userRepository.create(user)
        return userId
    }

    @Test
    fun `can create and fetch account`() {
        val userId = createTestUser()
        val account = makeAccount(userId = userId)
        accountRepository.create(account)
        val found = accountRepository.findByAccountNumber(account.accountNumber)
        assertNotNull(found)
        assertEquals(account.name, found?.name)
        assertEquals(account.balance, found?.balance)
    }

    @Test
    fun `user can have multiple accounts`() {
        val userId = createTestUser()
        val account1 = makeAccount(userId = userId)
        val account2 = makeAccount(userId = userId)
        accountRepository.create(account1)
        accountRepository.create(account2)
        val accounts = accountRepository.findAllByUserId(userId)
        assertEquals(2, accounts.size)
        val numbers = accounts.map { it.accountNumber }
        assertTrue(numbers.contains(account1.accountNumber))
        assertTrue(numbers.contains(account2.accountNumber))
    }

    @Test
    fun `update modifies account fields`() {
        val userId = createTestUser()
        val account = makeAccount(userId = userId)
        accountRepository.create(account)
        val updated = account.copy(name = "Updated Account", balance = 500.0, updatedTimestamp = Instant.now())
        accountRepository.update(updated)
        val fromDb = accountRepository.findByAccountNumber(account.accountNumber)
        assertEquals("Updated Account", fromDb?.name)
        assertEquals(500.0, fromDb?.balance)
    }

    @Test
    fun `delete removes the account`() {
        val userId = createTestUser()
        val account = makeAccount(userId = userId)
        accountRepository.create(account)
        accountRepository.delete(account.accountNumber)
        val found = accountRepository.findByAccountNumber(account.accountNumber)
        assertNull(found)
    }

    @Test
    fun `findByAccountNumber returns null for missing account`() {
        val found = accountRepository.findByAccountNumber("01999999")
        assertNull(found)
    }

    @Test
    fun `creating two accounts with same account number should fail`() {
        val userId = createTestUser()
        val accountNumber = "01${(1000000..9999999).random()}"
        val account1 = makeAccount(accountNumber = accountNumber, userId = userId)
        val account2 = makeAccount(accountNumber = accountNumber, userId = userId)
        accountRepository.create(account1)
        org.junit.jupiter.api.assertThrows<org.springframework.dao.DataIntegrityViolationException> {
            accountRepository.create(account2)
        }
    }

    @Test
    fun `creating an account for a nonexistent user should fail`() {
        val account = makeAccount(userId = "usr-nonexistent")
        org.junit.jupiter.api.assertThrows<org.springframework.dao.DataIntegrityViolationException> {
            accountRepository.create(account)
        }
    }
}
