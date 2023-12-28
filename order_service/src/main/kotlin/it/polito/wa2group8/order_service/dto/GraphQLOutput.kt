package it.polito.wa2group8.order_service.dto

//An object containing the result of an order deletion
data class DeletionResult(val result: Boolean, var message: String = "Deletion performed successfully")

//An object containing the result of an order status update
data class UpdateResult(val result: Boolean, var message: String = "Update performed successfully")