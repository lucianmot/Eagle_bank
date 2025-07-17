package com.eaglebank.repository

import com.eaglebank.model.Account
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp

@Repository
class AccountRepository(private val jdbc: JdbcTemplate) {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        Account(
            accountNumber = rs.getString("account_number"),
            userId = rs.getString("user_id"),
            name = rs.getString("name"),
            accountType = rs.getString("account_type"),
            balance = rs.getDouble("balance"),
            currency = rs.getString("currency"),
            sortCode = rs.getString("sort_code"),
            createdTimestamp = rs.getTimestamp("created_timestamp").toInstant(),
            updatedTimestamp = rs.getTimestamp("updated_timestamp").toInstant()
        )
    }

    fun create(account: Account): Int = jdbc.update(
        """
        INSERT INTO accounts (
            account_number, user_id, name, account_type, balance, currency, sort_code, created_timestamp, updated_timestamp
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent(),
        account.accountNumber,
        account.userId,
        account.name,
        account.accountType,
        account.balance,
        account.currency,
        account.sortCode,
        Timestamp.from(account.createdTimestamp),
        Timestamp.from(account.updatedTimestamp)
    )

    fun findByAccountNumber(accountNumber: String): Account? =
        jdbc.query(
            "SELECT * FROM accounts WHERE account_number = ?",
            rowMapper,
            accountNumber
        ).firstOrNull()

    fun findAllByUserId(userId: String): List<Account> =
        jdbc.query(
            "SELECT * FROM accounts WHERE user_id = ?",
            rowMapper,
            userId
        )

    fun update(account: Account): Int = jdbc.update(
        """
        UPDATE accounts SET
            name = ?, account_type = ?, balance = ?, currency = ?, sort_code = ?, updated_timestamp = ?
        WHERE account_number = ?
        """.trimIndent(),
        account.name,
        account.accountType,
        account.balance,
        account.currency,
        account.sortCode,
        Timestamp.from(account.updatedTimestamp),
        account.accountNumber
    )

    fun delete(accountNumber: String): Int =
        jdbc.update("DELETE FROM accounts WHERE account_number = ?", accountNumber)
}
