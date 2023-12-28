package it.polito.wa2group8.catalog_service.domain

import java.time.LocalDateTime

data class EmailVerificationToken(
    var expiryDate: LocalDateTime,
    var token: String,
    var username: String
): EntityBase<Long>()
