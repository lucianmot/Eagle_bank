package com.eaglebank.service

import com.eaglebank.exception.ConflictException
import com.eaglebank.exception.NotFoundException
import com.eaglebank.model.Account
import com.eaglebank.model.User
import com.eaglebank.repository.AccountRepository
import com.eaglebank.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant

class AccountServiceTest : BehaviorSpec({

    val accountRepository = mockk<AccountRepository>(relaxed = true)
    val userRepository = mockk<UserRepository>(relaxed = true)
    val accountService = AccountService(accountRepository, userRepository)

    fun makeUser(userId: String = "usr-123") = User(
        id = userId,
        name = "Test User",
        addressLine1 = "123 St",
        addressLine2 = null,
        addressLine3 = null,
        town = "Testtown",
        county = "Testshire",
        postcode = "TST 123",
        phoneNumber = "+441234567890",
        email = "test@example.com",
        passwordHash = "hash",
        createdTimestamp = Instant.now(),
        updatedTimestamp = Instant.now()
    )

    fun makeAccount(accountNumber: String = "01234567", userId: String = "usr-123") = Account(
        accountNumber = accountNumber,
        userId = userId,
        name = "Personal",
        accountType = "personal",
        balance = 100.0,
        currency = "GBP",
        sortCode = "10-10-10",
        createdTimestamp = Instant.now(),
        updatedTimestamp = Instant.now()
    )

    given("creating an account") {
        `when`("the user exists") {
            then("the account is created") {
                val user = makeUser()
                every { userRepository.findById(user.id) } returns user
                every { accountRepository.create(any()) } returns 1

                accountService.createAccount(makeAccount(userId = user.id))
                verify { accountRepository.create(any()) }
            }
        }
        `when`("the user does not exist") {
            then("throws NotFoundException") {
                every { userRepository.findById("usr-missing") } returns null
                shouldThrow<NotFoundException> {
                    accountService.createAccount(makeAccount(userId = "usr-missing"))
                }
            }
        }
    }

    given("fetching an account") {
        val user = makeUser()
        val account = makeAccount(userId = user.id)
        `when`("the account exists and belongs to the user") {
            every { accountRepository.findByAccountNumber(account.accountNumber) } returns account
            then("the account is returned") {
                accountService.getAccount(account.accountNumber, user.id) shouldBe account
            }
        }
        `when`("the account does not exist") {
            every { accountRepository.findByAccountNumber("99999999") } returns null
            then("throws NotFoundException") {
                shouldThrow<NotFoundException> {
                    accountService.getAccount("99999999", user.id)
                }
            }
        }
        `when`("the account exists but is not owned by user") {
            every {
                accountRepository.findByAccountNumber(account.accountNumber)
            } returns account.copy(userId = "usr-OTHER")
            then("throws ConflictException") {
                shouldThrow<ConflictException> {
                    accountService.getAccount(
                        account.accountNumber,
                        user.id
                    )
                }
            }
        }
    }

    given("updating an account") {
        val user = makeUser()
        val account = makeAccount(userId = user.id)
        `when`("the account exists and belongs to the user") {
            every { accountRepository.findByAccountNumber(account.accountNumber) } returns account
            every { accountRepository.update(any()) } returns 1
            then("the account is updated") {
                accountService.updateAccount(account.accountNumber, user.id, account.copy(name = "Updated"))
                verify { accountRepository.update(any()) }
            }
        }
        `when`("the account does not exist") {
            every { accountRepository.findByAccountNumber("99999999") } returns null
            then("throws NotFoundException") {
                shouldThrow<NotFoundException> {
                    accountService.updateAccount("99999999", user.id, account)
                }
            }
        }
        `when`("the account is not owned by user") {
            every {
                accountRepository.findByAccountNumber(account.accountNumber)
            } returns account.copy(userId = "usr-OTHER")
            then("throws ConflictException") {
                shouldThrow<ConflictException> {
                    accountService.updateAccount(
                        account.accountNumber,
                        user.id,
                        account
                    )
                }
            }
        }
    }

    given("deleting an account") {
        val user = makeUser()
        val account = makeAccount(userId = user.id)
        `when`("the account exists and belongs to the user") {
            every { accountRepository.findByAccountNumber(account.accountNumber) } returns account
            every { accountRepository.delete(account.accountNumber) } returns 1
            then("the account is deleted") {
                accountService.deleteAccount(account.accountNumber, user.id)
                verify { accountRepository.delete(account.accountNumber) }
            }
        }
        `when`("the account does not exist") {
            every { accountRepository.findByAccountNumber("99999999") } returns null
            then("throws NotFoundException") {
                shouldThrow<NotFoundException> {
                    accountService.deleteAccount("99999999", user.id)
                }
            }
        }
        `when`("the account is not owned by user") {
            every {
                accountRepository.findByAccountNumber(account.accountNumber)
            } returns account.copy(userId = "usr-OTHER")
            then("throws ConflictException") {
                shouldThrow<ConflictException> {
                    accountService.deleteAccount(
                        account.accountNumber,
                        user.id
                    )
                }
            }
        }
    }

    given("fetching all accounts for a user") {
        val userId = "usr-123"
        val accounts = listOf(
            makeAccount(accountNumber = "01111111", userId = userId),
            makeAccount(accountNumber = "02222222", userId = userId)
        )
        every { accountRepository.findAllByUserId(userId) } returns accounts
        then("all accounts are returned") {
            accountService.getAllAccountsForUser(userId) shouldBe accounts
        }
    }
})
