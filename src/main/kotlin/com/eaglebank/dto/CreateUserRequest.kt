package com.eaglebank.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    @field:NotBlank val name: String?,
    @field:NotBlank val addressLine1: String?,
    val addressLine2: String?,
    val addressLine3: String?,
    @field:NotBlank val town: String?,
    @field:NotBlank val county: String?,
    @field:NotBlank val postcode: String?,
    @field:NotBlank val phoneNumber: String?,
    @field:NotBlank @field:Email val email: String?,
    @field:NotBlank val password: String?
)
