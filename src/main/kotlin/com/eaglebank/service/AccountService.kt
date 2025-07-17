@file:Suppress("UnusedPrivateProperty")

package com.eaglebank.service

import com.eaglebank.exception.ConflictException
import com.eaglebank.exception.NotFoundException
import com.eaglebank.model.Account
import com.eaglebank.repository.AccountRepository
import com.eaglebank.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(AccountService::class.java)

    fun createAccount(account: Account) {
        // Check user exists before creating account (defensive, DB also enforces)
        val user = userRepository.findById(account.userId)
            ?: throw NotFoundException("User not found for id=${account.userId}")
        log.info("Creating account {} for user {}", account.accountNumber, account.userId)
        accountRepository.create(account)
    }

    fun getAccount(accountNumber: String, userId: String): Account {
        val account = accountRepository.findByAccountNumber(accountNumber)
            ?: throw NotFoundException("Account not found: $accountNumber")
        if (account.userId != userId) {
            log.warn("User {} attempted to access account {} not owned by them", userId, accountNumber)
            throw ConflictException("Account does not belong to this user.")
        }
        return account
    }

    fun getAllAccountsForUser(userId: String): List<Account> =
        accountRepository.findAllByUserId(userId)

    fun updateAccount(accountNumber: String, userId: String, updated: Account) {
        val existing = accountRepository.findByAccountNumber(accountNumber)
            ?: throw NotFoundException("Account not found: $accountNumber")
        if (existing.userId != userId) {
            log.warn("User {} attempted to update account {} not owned by them", userId, accountNumber)
            throw ConflictException("Account does not belong to this user.")
        }
        val accountToUpdate = updated.copy(
            accountNumber = accountNumber,
            userId = userId,
            updatedTimestamp = Instant.now()
        )
        accountRepository.update(accountToUpdate)
        log.info("Updated account {} for user {}", accountNumber, userId)
    }

    fun deleteAccount(accountNumber: String, userId: String) {
        val account = accountRepository.findByAccountNumber(accountNumber)
            ?: throw NotFoundException("Account not found: $accountNumber")
        if (account.userId != userId) {
            log.warn("User {} attempted to delete account {} not owned by them", userId, accountNumber)
            throw ConflictException("Account does not belong to this user.")
        }
        accountRepository.delete(accountNumber)
        log.info("Deleted account {} for user {}", accountNumber, userId)
    }
}
