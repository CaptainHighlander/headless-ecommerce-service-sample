package it.polito.wa2group8.catalog_service.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

data class RegistrationRequestDTO
(
    @get:NotEmpty val username: String,
    @get:Size(min = 8) val password: String,
    @get:Size(min = 8) val confirmPassword: String,
    @get:NotEmpty val city: String,
    @get:NotEmpty val street: String,
    @get:NotEmpty val zip: String,
    @field:Email(regexp = ".+@.+\\..+") @field:NotBlank val email: String,
)
