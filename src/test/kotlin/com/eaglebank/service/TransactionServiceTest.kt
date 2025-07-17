package com.eaglebank.service
import com.eaglebank.exception.ConflictException
import com.eaglebank.exception.NotFoundException
import com.eaglebank.exception.UnprocessableEntityException
import com.eaglebank.model.Account
import com.eaglebank.model.Transaction
import com.eaglebank.model.TransactionType
import com.eaglebank.repository.AccountRepository
import com.eaglebank.repository.TransactionRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant

class TransactionServiceTest : BehaviorSpec({

    val transactionRepository = mockk<TransactionRepository>(relaxed = true)
    val accountRepository = mockk<AccountRepository>(relaxed = true)
    val transactionService = TransactionService(transactionRepository, accountRepository)

    fun makeAccount(accountNumber: String = "01234567", userId: String = "usr-123", balance: Double = 100.0) = Account(
        accountNumber = accountNumber,
        userId = userId,
        name = "Personal",
        accountType = "personal",
        balance = balance,
        currency = "GBP",
        sortCode = "10-10-10",
        createdTimestamp = Instant.now(),
        updatedTimestamp = Instant.now()
    )

    fun makeTransaction(
        id: String = "tan-1",
        type: TransactionType = TransactionType.DEPOSIT,
        amount: Double = 50.0,
        accountNumber: String = "01234567",
        userId: String = "usr-123"
    ) = Transaction(
        id = id,
        accountNumber = accountNumber,
        userId = userId,
        amount = amount,
        currency = "GBP",
        type = type,
        reference = "Test",
        createdTimestamp = Instant.now()
    )

    given("creating a deposit") {
        val userId = "usr-123"
        val account = makeAccount(userId = userId, balance = 100.0)
        val deposit = makeTransaction(
            type = TransactionType.DEPOSIT,
            amount = 40.0,
            userId = userId,
            accountNumber = account.accountNumber
        )
        `when`("the account exists and is owned by the user") {
            every { accountRepository.findByAccountNumber(account.accountNumber) } returns account
            every { accountRepository.update(any()) } returns 1
            every { transactionRepository.create(any()) } returns 1
            then("the deposit increases the balance and creates a transaction") {
                transactionService.createTransaction(
                    account.accountNumber,
                    userId,
                    deposit
                )
                verify {
                    accountRepository.update(
                        match { it.balance == 140.0 }
                    )
                }
                verify {
                    transactionRepository.create(any())
                }
            }
        }
        `when`("the account does not exist") {
            every { accountRepository.findByAccountNumber("99999999") } returns null
            then("throws NotFoundException") {
                shouldThrow<NotFoundException> {
                    transactionService.createTransaction("99999999", userId, deposit)
                }
            }
        }
        `when`("the account is not owned by user") {
            every {
                accountRepository.findByAccountNumber(account.accountNumber)
            } returns account.copy(userId = "usr-other")
            then("throws ConflictException") {
                shouldThrow<ConflictException> {
                    transactionService.createTransaction(account.accountNumber, userId, deposit)
                }
            }
        }
    }

    given("creating a withdrawal") {
        val userId = "usr-123"
        val account = makeAccount(userId = userId, balance = 100.0)
        val withdrawal = makeTransaction(
            type = TransactionType.WITHDRAWAL,
            amount = 80.0,
            userId = userId,
            accountNumber = account.accountNumber
        )
        `when`("the account exists, is owned, and has enough funds") {
            every { accountRepository.findByAccountNumber(account.accountNumber) } returns account
            every { accountRepository.update(any()) } returns 1
            every { transactionRepository.create(any()) } returns 1
            then("the withdrawal decreases the balance and creates a transaction") {
                transactionService.createTransaction(
                    account.accountNumber,
                    userId,
                    withdrawal
                )
                verify {
                    accountRepository.update(
                        match { it.balance == 20.0 }
                    )
                }
                verify {
                    transactionRepository.create(any())
                }
            }
        }
        `when`("the account does not exist") {
            every { accountRepository.findByAccountNumber("99999999") } returns null
            then("throws NotFoundException") {
                shouldThrow<NotFoundException> {
                    transactionService.createTransaction("99999999", userId, withdrawal)
                }
            }
        }
        `when`("the account is not owned by user") {
            every {
                accountRepository.findByAccountNumber(account.accountNumber)
            } returns account.copy(userId = "usr-other")
            then("throws ConflictException") {
                shouldThrow<ConflictException> {
                    transactionService.createTransaction(account.accountNumber, userId, withdrawal)
                }
            }
        }
        `when`("the withdrawal would overdraw the account") {
            every { accountRepository.findByAccountNumber(account.accountNumber) } returns account
            val overdraw = withdrawal.copy(amount = 120.0)
            then("throws UnprocessableEntityException") {
                shouldThrow<UnprocessableEntityException> {
                    transactionService.createTransaction(
                        account.accountNumber,
                        userId,
                        overdraw
                    )
                }
            }
        }
    }

    given("fetching transactions for an account") {
        val userId = "usr-123"
        val account = makeAccount(userId = userId)
        val transactions = listOf(
            makeTransaction(
                accountNumber = account.accountNumber,
                userId = userId
            )
        )
        `when`("the account exists and is owned by the user") {
            every { accountRepository.findByAccountNumber(account.accountNumber) } returns account
            every {
                transactionRepository.findAllByAccountNumber(account.accountNumber)
            } returns transactions
            then("the transactions are returned") {
                transactionService.getTransactions(
                    account.accountNumber,
                    userId
                ) shouldBe transactions
            }
        }
        `when`("the account does not exist") {
            every { accountRepository.findByAccountNumber("99999999") } returns null
            then("throws NotFoundException") {
                shouldThrow<NotFoundException> {
                    transactionService.getTransactions("99999999", userId)
                }
            }
        }
        `when`("the account is not owned by the user") {
            every {
                accountRepository.findByAccountNumber(account.accountNumber)
            } returns account.copy(userId = "usr-other")
            then("throws ConflictException") {
                shouldThrow<ConflictException> {
                    transactionService.getTransactions(
                        account.accountNumber,
                        userId
                    )
                }
            }
        }
    }

    given("fetching a single transaction") {
        val userId = "usr-123"
        val account = makeAccount(userId = userId)
        val tx = makeTransaction(
            accountNumber = account.accountNumber,
            userId = userId
        )
        `when`("the account and transaction exist and are owned") {
            every { accountRepository.findByAccountNumber(account.accountNumber) } returns account
            every {
                transactionRepository.findByIdAndAccountNumber(
                    tx.id,
                    account.accountNumber
                )
            } returns tx
            then("the transaction is returned") {
                transactionService.getTransactionById(
                    account.accountNumber,
                    userId,
                    tx.id
                ) shouldBe tx
            }
        }
        `when`("the account does not exist") {
            every { accountRepository.findByAccountNumber("99999999") } returns null
            then("throws NotFoundException") {
                shouldThrow<NotFoundException> {
                    transactionService.getTransactionById("99999999", userId, tx.id)
                }
            }
        }
        `when`("the account is not owned by the user") {
            every {
                accountRepository.findByAccountNumber(account.accountNumber)
            } returns account.copy(userId = "usr-other")
            then("throws ConflictException") {
                shouldThrow<ConflictException> {
                    transactionService.getTransactionById(
                        account.accountNumber,
                        userId,
                        tx.id
                    )
                }
            }
        }
        `when`("the transaction does not exist") {
            every {
                accountRepository.findByAccountNumber(account.accountNumber)
            } returns account
            every {
                transactionRepository.findByIdAndAccountNumber(
                    "tan-missing",
                    account.accountNumber
                )
            } returns null
            then("throws NotFoundException") {
                shouldThrow<NotFoundException> {
                    transactionService.getTransactionById(
                        account.accountNumber,
                        userId,
                        "tan-missing"
                    )
                }
            }
        }
    }
})
