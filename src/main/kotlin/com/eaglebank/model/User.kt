package com.eaglebank.model

import java.time.Instant
import java.util.UUID

data class User(
    val id: String = "usr-" + UUID.randomUUID().toString(),
    val name: String,
    val addressLine1: String,
    val addressLine2: String?,
    val addressLine3: String?,
    val town: String,
    val county: String,
    val postcode: String,
    val phoneNumber: String,
    val email: String,
    val passwordHash: String,
    val createdTimestamp: Instant,
    val updatedTimestamp: Instant
)
