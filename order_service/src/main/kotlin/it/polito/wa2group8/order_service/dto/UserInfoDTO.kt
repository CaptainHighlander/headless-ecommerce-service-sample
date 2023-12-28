package it.polito.wa2group8.order_service.dto

//Contains the information about a user that make a request to the Order microservice.
//Information are extracted by the JWT received by catalog microservice.
data class UserInfoDTO(
    val username: String,
    val city: String,
    val street: String,
    val zip: String,
    val roles: ArrayList<String>
)
