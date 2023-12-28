package it.polito.wa2group8.catalog_service.services

import it.polito.wa2group8.catalog_service.dto.PasswordDTO
import it.polito.wa2group8.catalog_service.dto.RegistrationRequestDTO
import it.polito.wa2group8.catalog_service.dto.UserDetailsDTO
import it.polito.wa2group8.catalog_service.dto.UserInfoDTO

interface UserDetailsService
{
    suspend fun findByUsername(username: String): UserDetailsDTO
    suspend fun createUser(registrationRequest: RegistrationRequestDTO): UserDetailsDTO
    suspend fun addRoleToUser(role: String, username: String)
    suspend fun removeRoleToUser(role: String, username: String)
    suspend fun enableUser(username: String)
    suspend fun disableUser(username: String)
    suspend fun confirmRegistration(token: String): String
    suspend fun getUserInfo(token: String): UserInfoDTO
    suspend fun updateUserInfo(token: String, newInfo: UserInfoDTO)
    suspend fun changePassword(token: String, passwordDTO: PasswordDTO)
}
