@file:Suppress("ImportOrdering")

package com.eaglebank.service

import java.time.Instant
import com.eaglebank.exception.ConflictException
import com.eaglebank.exception.DuplicateUserException
import com.eaglebank.exception.NotFoundException
import com.eaglebank.model.User
import com.eaglebank.repository.AccountRepository
import com.eaglebank.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.dao.DataIntegrityViolationException

class UserServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>(relaxed = true)
    val accountRepository = mockk<AccountRepository>(relaxed = true)
    val userService = UserService(userRepository, accountRepository)

    fun makeUser(id: String = "usr-123") = User(
        id = id,
        name = "Test User",
        addressLine1 = "Test 1",
        addressLine2 = null,
        addressLine3 = null,
        town = "Town",
        county = "County",
        postcode = "POST",
        phoneNumber = "+441234567890",
        email = "test@example.com",
        passwordHash = "hash",
        createdTimestamp = Instant.now(),
        updatedTimestamp = Instant.now()
    )

    given("a new user registration") {
        `when`("the email is not taken") {
            then("the user is created") {
                every { userRepository.create(any()) } returns 1
                userService.createUser(makeUser())
                verify(exactly = 1) { userRepository.create(any()) }
            }
        }
        `when`("the email is already registered") {
            then("a DuplicateUserException is thrown") {
                every { userRepository.create(any()) } throws DataIntegrityViolationException("dupe")
                shouldThrow<DuplicateUserException> {
                    userService.createUser(
                        makeUser()
                    )
                }
            }
        }
    }

    given("fetching a user") {
        val user = makeUser()
        every { userRepository.findById(user.id) } returns user
        `when`("the user exists") {
            then("the user is returned") {
                userService.getUser(user.id) shouldBe user
            }
        }
        `when`("the user does not exist") {
            every { userRepository.findById("missing") } returns null
            then("null is returned") {
                userService.getUser("missing") shouldBe null
            }
        }
    }

    given("updating a user") {
        val user = makeUser()
        `when`("the user does not exist") {
            every { userRepository.findById(user.id) } returns null
            then("a NotFoundException is thrown") {
                shouldThrow<NotFoundException> {
                    userService.updateUser(user.id, user)
                }
            }
        }
        `when`("the user exists") {
            every { userRepository.findById(user.id) } returns user
            every { userRepository.update(any()) } returns 1
            then("the user is updated") {
                userService.updateUser(user.id, user.copy(name = "Changed"))
                verify { userRepository.update(any()) }
            }
        }
    }

    given("deleting a user") {
        val userId = "usr-123"
        `when`("the user has accounts") {
            every { accountRepository.findAllByUserId(userId) } returns listOf(mockk())
            then("a ConflictException is thrown") {
                shouldThrow<ConflictException> {
                    userService.deleteUser(userId)
                }
            }
        }
        `when`("the user has no accounts") {
            every { accountRepository.findAllByUserId(userId) } returns emptyList()
            every { userRepository.delete(userId) } returns 1
            then("the user is deleted") {
                userService.deleteUser(userId)
                verify { userRepository.delete(userId) }
            }
        }
    }
})
