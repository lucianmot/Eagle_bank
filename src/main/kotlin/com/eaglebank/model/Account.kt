package com.eaglebank.model

import java.time.Instant

data class Account(
    val accountNumber: String,
    val userId: String,
    val name: String,
    val accountType: String,
    val balance: Double,
    val currency: String,
    val sortCode: String,
    val createdTimestamp: Instant,
    val updatedTimestamp: Instant
)
