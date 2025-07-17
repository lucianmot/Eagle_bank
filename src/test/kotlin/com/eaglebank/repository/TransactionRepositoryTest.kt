package com.eaglebank.repository

import com.eaglebank.model.Account
import com.eaglebank.model.Transaction
import com.eaglebank.model.TransactionType
import com.eaglebank.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class TransactionRepositoryTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    private fun createTestUser(userId: String = "usr-" + UUID.randomUUID()): User {
        val now = Instant.now()
        val user = User(
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
        return user
    }

    private fun createTestAccount(user: User, accountNumber: String = "01${(1000000..9999999).random()}"): Account {
        val now = Instant.now()
        val account = Account(
            accountNumber = accountNumber,
            userId = user.id,
            name = "Test Account",
            accountType = "personal",
            balance = 1000.0,
            currency = "GBP",
            sortCode = "10-10-10",
            createdTimestamp = now,
            updatedTimestamp = now
        )
        accountRepository.create(account)
        return account
    }

    private fun makeTransaction(
        account: Account,
        user: User,
        type: TransactionType = TransactionType.DEPOSIT,
        amount: Double = 100.0
    ): Transaction {
        return Transaction(
            id = "tan-" + UUID.randomUUID(),
            accountNumber = account.accountNumber,
            userId = user.id,
            amount = amount,
            currency = "GBP",
            type = type,
            reference = "Test reference",
            createdTimestamp = Instant.now()
        )
    }

    @Test
    fun `can create and fetch transaction by id and account`() {
        val user = createTestUser()
        val account = createTestAccount(user)
        val transaction = makeTransaction(account, user, TransactionType.DEPOSIT)
        transactionRepository.create(transaction)
        val found = transactionRepository.findByIdAndAccountNumber(transaction.id, account.accountNumber)
        assertNotNull(found)
        assertEquals(transaction.amount, found?.amount)
        assertEquals(TransactionType.DEPOSIT, found?.type)
    }

    @Test
    fun `findAllByAccountNumber returns all transactions for account`() {
        val user = createTestUser()
        val account = createTestAccount(user)
        val tx1 = makeTransaction(account, user, TransactionType.DEPOSIT, 100.0)
        val tx2 = makeTransaction(account, user, TransactionType.WITHDRAWAL, 50.0)
        transactionRepository.create(tx1)
        transactionRepository.create(tx2)
        val transactions = transactionRepository.findAllByAccountNumber(account.accountNumber)
        assertEquals(2, transactions.size)
        val types = transactions.map { it.type }
        assertTrue(types.contains(TransactionType.DEPOSIT))
        assertTrue(types.contains(TransactionType.WITHDRAWAL))
    }

    @Test
    fun `findByIdAndAccountNumber returns null for missing transaction`() {
        val user = createTestUser()
        val account = createTestAccount(user)
        val found = transactionRepository.findByIdAndAccountNumber("tan-doesnotexist", account.accountNumber)
        assertNull(found)
    }

    @Test
    fun `creating a transaction for nonexistent account should fail`() {
        val user = createTestUser()
        val transaction = Transaction(
            id = "tan-" + UUID.randomUUID(),
            accountNumber = "01234567", // Not created
            userId = user.id,
            amount = 100.0,
            currency = "GBP",
            type = TransactionType.DEPOSIT,
            reference = "Test",
            createdTimestamp = Instant.now()
        )
        assertThrows<DataIntegrityViolationException> {
            transactionRepository.create(transaction)
        }
    }
}
