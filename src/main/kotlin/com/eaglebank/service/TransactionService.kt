package com.eaglebank.service

import com.eaglebank.exception.ConflictException
import com.eaglebank.exception.NotFoundException
import com.eaglebank.exception.UnprocessableEntityException
import com.eaglebank.model.Transaction
import com.eaglebank.model.TransactionType
import com.eaglebank.repository.AccountRepository
import com.eaglebank.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) {
    private val log = LoggerFactory.getLogger(TransactionService::class.java)

    private fun requireAccountOwnedByUser(accountNumber: String, userId: String): com.eaglebank.model.Account {
        val account = accountRepository.findByAccountNumber(accountNumber)
            ?: throw NotFoundException("Account not found: $accountNumber")
        if (account.userId != userId) {
            log.warn("User {} attempted to access account {} not owned by them", userId, accountNumber)
            throw ConflictException("Account does not belong to this user.")
        }
        return account
    }

    @Transactional
    fun createTransaction(accountNumber: String, userId: String, transaction: Transaction) {
        val account = requireAccountOwnedByUser(accountNumber, userId)

        val now = Instant.now()
        when (transaction.type) {
            TransactionType.DEPOSIT -> {
                val newBalance = account.balance + transaction.amount
                val updatedAccount = account.copy(
                    balance = newBalance,
                    updatedTimestamp = now
                )
                accountRepository.update(updatedAccount)
                transactionRepository.create(
                    transaction.copy(
                        accountNumber = accountNumber,
                        userId = userId,
                        createdTimestamp = now
                    )
                )
                log.info("User {} deposited {} to account {}", userId, transaction.amount, accountNumber)
            }
            TransactionType.WITHDRAWAL -> {
                if (account.balance < transaction.amount) {
                    log.warn("User {} attempted to overdraw account {}", userId, accountNumber)
                    throw UnprocessableEntityException("Insufficient funds.")
                }
                val newBalance = account.balance - transaction.amount
                val updatedAccount = account.copy(
                    balance = newBalance,
                    updatedTimestamp = now
                )
                accountRepository.update(updatedAccount)
                transactionRepository.create(
                    transaction.copy(
                        accountNumber = accountNumber,
                        userId = userId,
                        createdTimestamp = now
                    )
                )
                log.info("User {} withdrew {} from account {}", userId, transaction.amount, accountNumber)
            }
        }
    }

    fun getTransactions(accountNumber: String, userId: String): List<Transaction> {
        val account = requireAccountOwnedByUser(accountNumber, userId)
        return transactionRepository.findAllByAccountNumber(accountNumber)
    }

    fun getTransactionById(accountNumber: String, userId: String, transactionId: String): Transaction {
        val account = requireAccountOwnedByUser(accountNumber, userId)
        return transactionRepository.findByIdAndAccountNumber(transactionId, accountNumber)
            ?: throw NotFoundException("Transaction not found: $transactionId for account $accountNumber")
    }
}
