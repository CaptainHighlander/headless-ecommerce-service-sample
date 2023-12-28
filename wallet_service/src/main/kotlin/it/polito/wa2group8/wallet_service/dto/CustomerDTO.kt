package it.polito.wa2group8.wallet_service.dto

import it.polito.wa2group8.wallet_service.domain.Customer
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class CustomerDTO(
    @get:NotNull(message="Invalid customerId") @get:Positive(message="Invalid customerId") val customerId: Long?,
    val username: String?
)

fun Customer.toCustomerDTO() = CustomerDTO(this.getId(), username)
