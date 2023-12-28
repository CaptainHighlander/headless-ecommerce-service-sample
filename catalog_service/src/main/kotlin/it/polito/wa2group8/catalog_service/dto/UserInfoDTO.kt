package it.polito.wa2group8.catalog_service.dto

import it.polito.wa2group8.catalog_service.domain.User

data class UserInfoDTO(
    val email: String?,
    val city: String?,
    val street: String?,
    val zip: String?,
)

fun divideAddress(address: String): List<String>
{
    return address.split("\n")
}

fun UserDetailsDTO.toUserInfoDTO() = UserInfoDTO(getEmail(), divideAddress(getDeliveryAddress())[0],divideAddress(getDeliveryAddress())[1],divideAddress(getDeliveryAddress())[2])

fun User.toUserInfoDTO() = UserInfoDTO(email, city, street, zip)
