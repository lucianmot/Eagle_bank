package com.eaglebank.repository

import com.eaglebank.model.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp

@Repository
class UserRepository(private val jdbc: JdbcTemplate) {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        User(
            id = rs.getString("id"),
            name = rs.getString("name"),
            addressLine1 = rs.getString("address_line1"),
            addressLine2 = rs.getString("address_line2"),
            addressLine3 = rs.getString("address_line3"),
            town = rs.getString("town"),
            county = rs.getString("county"),
            postcode = rs.getString("postcode"),
            phoneNumber = rs.getString("phone_number"),
            email = rs.getString("email"),
            passwordHash = rs.getString("password_hash"),
            createdTimestamp = rs.getTimestamp("created_timestamp").toInstant(),
            updatedTimestamp = rs.getTimestamp("updated_timestamp").toInstant()
        )
    }

    fun create(user: User): Int = jdbc.update(
        """
        INSERT INTO users (
            id, name, address_line1, address_line2, address_line3, town, county, postcode, 
            phone_number, email, password_hash, created_timestamp, updated_timestamp
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent(),
        user.id, user.name, user.addressLine1, user.addressLine2, user.addressLine3,
        user.town, user.county, user.postcode, user.phoneNumber, user.email, user.passwordHash,
        Timestamp.from(user.createdTimestamp), Timestamp.from(user.updatedTimestamp)
    )

    fun findById(userId: String): User? =
        jdbc.query("SELECT * FROM users WHERE id = ?", rowMapper, userId).firstOrNull()

    fun findByEmail(email: String): User? =
        jdbc.query("SELECT * FROM users WHERE email = ?", rowMapper, email).firstOrNull()

    fun update(user: User): Int = jdbc.update(
        """
        UPDATE users SET
            name = ?, address_line1 = ?, address_line2 = ?, address_line3 = ?, town = ?, county = ?, postcode = ?,
            phone_number = ?, email = ?, password_hash = ?, updated_timestamp = ?
        WHERE id = ?
        """.trimIndent(),
        user.name, user.addressLine1, user.addressLine2, user.addressLine3,
        user.town, user.county, user.postcode, user.phoneNumber, user.email, user.passwordHash,
        Timestamp.from(user.updatedTimestamp), user.id
    )

    fun delete(userId: String): Int =
        jdbc.update("DELETE FROM users WHERE id = ?", userId)
}
