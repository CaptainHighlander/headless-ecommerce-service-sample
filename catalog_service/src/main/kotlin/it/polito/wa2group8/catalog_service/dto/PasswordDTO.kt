package it.polito.wa2group8.catalog_service.dto

import org.jetbrains.annotations.NotNull

data class PasswordDTO(
    @NotNull val oldPassword: String,
    @NotNull val newPassword: String,
    @NotNull val confirmPassword: String
)
