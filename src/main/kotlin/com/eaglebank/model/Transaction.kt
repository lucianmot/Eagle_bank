package com.eaglebank.model

import java.time.Instant

data class Transaction(
    val id: String,
    val accountNumber: String,
    val userId: String,
    val amount: Double,
    val currency: String,
    val type: TransactionType,
    val reference: String?,
    val createdTimestamp: Instant
)

enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL;
    companion object {
        fun fromDbValue(value: String): TransactionType =
            when (value.lowercase()) {
                "deposit" -> DEPOSIT
                "withdrawal" -> WITHDRAWAL
                else -> throw IllegalArgumentException("Unknown type: $value")
            }
        fun toDbValue(type: TransactionType): String = type.name.lowercase()
    }
}
