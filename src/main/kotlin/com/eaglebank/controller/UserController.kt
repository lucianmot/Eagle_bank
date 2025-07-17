@file:Suppress("ImportOrdering")

package com.eaglebank.controller

import com.eaglebank.dto.CreateUserRequest
import com.eaglebank.exception.ConflictException
import com.eaglebank.exception.NotFoundException
import com.eaglebank.model.User
import com.eaglebank.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.Valid

@RestController
@RequestMapping("/v1/users")
@Validated
class UserController(
    private val userService: UserService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }

    // Create user (sign up)
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<Map<String, String>> {
        val user = User(
            id = java.util.UUID.randomUUID().toString(),
            name = request.name!!,
            addressLine1 = request.addressLine1!!,
            addressLine2 = request.addressLine2,
            addressLine3 = request.addressLine3,
            town = request.town!!,
            county = request.county!!,
            postcode = request.postcode!!,
            phoneNumber = request.phoneNumber!!,
            email = request.email!!,
            passwordHash = request.password!!,
            createdTimestamp = java.time.Instant.now(),
            updatedTimestamp = java.time.Instant.now()
        )
        userService.createUser(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("id" to user.id))
    }

    // Fetch own user details
    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: String, @AuthenticationPrincipal jwt: Jwt): ResponseEntity<User> {
        if (userId != jwt.subject) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val user = userService.getUser(userId)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    // Update own user details
    @PatchMapping("/{userId}")
    fun updateUser(
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<Void> {
        val response: ResponseEntity<Void>
        if (userId != jwt.subject) {
            response = ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } else {
            val user = User(
                id = userId,
                name = request.name!!,
                addressLine1 = request.addressLine1!!,
                addressLine2 = request.addressLine2,
                addressLine3 = request.addressLine3,
                town = request.town!!,
                county = request.county!!,
                postcode = request.postcode!!,
                phoneNumber = request.phoneNumber!!,
                email = request.email!!,
                passwordHash = request.password!!,
                createdTimestamp = java.time.Instant.now(),
                updatedTimestamp = java.time.Instant.now()
            )
            response = try {
                userService.updateUser(userId, user)
                ResponseEntity.ok().build()
            } catch (e: NotFoundException) {
                logger.warn("User not found in updateUser: $userId", e)
                ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            }
        }
        return response
    }

    // Delete own user
    @DeleteMapping("/{userId}")
    fun deleteUser(
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<Void> {
        val response: ResponseEntity<Void>
        if (userId != jwt.subject) {
            response = ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } else {
            response = try {
                userService.deleteUser(userId)
                ResponseEntity.noContent().build()
            } catch (e: ConflictException) {
                logger.warn("Conflict deleting user: $userId", e)
                ResponseEntity.status(HttpStatus.CONFLICT).build()
            } catch (e: NotFoundException) {
                logger.warn("User not found in deleteUser: $userId", e)
                ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            }
        }
        return response
    }
}
