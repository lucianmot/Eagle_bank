@file:Suppress("UnusedPrivateProperty")

package com.eaglebank.service

import com.eaglebank.exception.ConflictException
import com.eaglebank.exception.DuplicateUserException
import com.eaglebank.exception.NotFoundException
import com.eaglebank.model.User
import com.eaglebank.repository.AccountRepository
import com.eaglebank.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository
) {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    fun createUser(user: User) {
        log.info("Creating user with email={}", user.email)
        try {
            userRepository.create(user)
        } catch (e: DataIntegrityViolationException) {
            log.warn("Attempt to create duplicate user with email={}", user.email)
            throw DuplicateUserException("Email already registered.", e)
        }
    }

    fun getUser(userId: String): User? =
        userRepository.findById(userId)

    fun updateUser(userId: String, updated: User) {
        val existing = userRepository.findById(userId)
            ?: throw NotFoundException("User not found")
        // Optionally: add checks here to restrict what can be updated
        val userToUpdate = updated.copy(id = userId, updatedTimestamp = Instant.now())
        userRepository.update(userToUpdate)
        log.info("Updated user: {}", userId)
    }

    fun deleteUser(userId: String) {
        val accounts = accountRepository.findAllByUserId(userId)
        if (accounts.isNotEmpty()) {
            log.warn("User {} attempted to delete but still has accounts.", userId)
            throw ConflictException("Cannot delete user with active accounts.")
        }
        userRepository.delete(userId)
        log.info("Deleted user: {}", userId)
    }
}
