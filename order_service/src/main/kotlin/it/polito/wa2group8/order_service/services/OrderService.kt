package it.polito.wa2group8.order_service.services

import graphql.schema.DataFetchingFieldSelectionSet
import it.polito.wa2group8.order_service.dto.*

interface OrderService
{
    fun createOrder(orderInput: OrderInput, userInfoDTO: UserInfoDTO): Long?
    fun updateOrderStatus(orderUpdateInput: OrderUpdateInput, userInfo: UserInfoDTO): UpdateResult
    fun deleteOrderById(orderId: Long, userInfo: UserInfoDTO): DeletionResult
    fun getAllOrders(userInfoDTO: UserInfoDTO, selectionSet: DataFetchingFieldSelectionSet): List<OrderDTO>
    fun getOrdersByUser(username: String, selectionSet: DataFetchingFieldSelectionSet): List<OrderDTO>
    fun getOrderById(orderId: Long, userInfoDTO: UserInfoDTO, selectionSet: DataFetchingFieldSelectionSet): OrderDTO?
}
