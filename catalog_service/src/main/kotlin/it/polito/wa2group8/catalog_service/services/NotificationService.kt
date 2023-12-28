package it.polito.wa2group8.catalog_service.services

import it.polito.wa2group8.catalog_service.domain.User

interface NotificationService
{
    suspend fun createEmailVerificationToken(user: User) : String
}
