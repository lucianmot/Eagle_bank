package com.eaglebank.repository

import com.eaglebank.model.Transaction
import com.eaglebank.model.TransactionType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp

@Repository
class TransactionRepository(private val jdbc: JdbcTemplate) {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        Transaction(
            id = rs.getString("id"),
            accountNumber = rs.getString("account_number"),
            userId = rs.getString("user_id"),
            amount = rs.getDouble("amount"),
            currency = rs.getString("currency"),
            type = TransactionType.fromDbValue(rs.getString("type")),
            reference = rs.getString("reference"),
            createdTimestamp = rs.getTimestamp("created_timestamp").toInstant()
        )
    }

    fun create(transaction: Transaction): Int = jdbc.update(
        """
        INSERT INTO transactions (
            id, account_number, user_id, amount, currency, type, reference, created_timestamp
        ) VALUES (?, ?, ?, ?, ?, ?::transaction_type, ?, ?)
        """.trimIndent(),
        transaction.id, transaction.accountNumber, transaction.userId, transaction.amount,
        transaction.currency, TransactionType.toDbValue(transaction.type), transaction.reference,
        Timestamp.from(transaction.createdTimestamp)
    )

    fun findByIdAndAccountNumber(id: String, accountNumber: String): Transaction? =
        jdbc.query(
            "SELECT * FROM transactions WHERE id = ? AND account_number = ?",
            rowMapper,
            id,
            accountNumber
        ).firstOrNull()

    fun findAllByAccountNumber(accountNumber: String): List<Transaction> =
        jdbc.query(
            "SELECT * FROM transactions WHERE account_number = ? ORDER BY created_timestamp",
            rowMapper,
            accountNumber
        )
}
