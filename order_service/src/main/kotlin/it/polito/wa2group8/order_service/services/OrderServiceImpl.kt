package it.polito.wa2group8.order_service.services

import graphql.schema.DataFetchingFieldSelectionSet
import it.polito.wa2group8.order_service.domain.OrderStatus
import it.polito.wa2group8.order_service.dto.*
import it.polito.wa2group8.order_service.exceptions.ForbiddenException
import it.polito.wa2group8.order_service.repositories.DeliveryRepository
import it.polito.wa2group8.order_service.repositories.OrderRepository
import it.polito.wa2group8.order_service.repositories.PurchasedProductRepository
import it.polito.wa2group8.order_service.saga_outbox.OrderSagaManager
import it.polito.wa2group8.order_service.saga_outbox.events.EmailEvent
import it.polito.wa2group8.order_service.saga_outbox.events.TO_CATALOG_TOPIC
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@EnableKafka
class OrderServiceImpl(
    val orderRepository: OrderRepository,
    private val purchasedProductRepository: PurchasedProductRepository,
    private val deliveryRepository: DeliveryRepository,
    private val orderSagaManager: OrderSagaManager,
    private val kafkaTemplate: KafkaTemplate<String, String>,
): OrderService
{
    override fun createOrder(orderInput: OrderInput, userInfoDTO: UserInfoDTO): Long?
    {
        //Create a new order using patterns Saga and Outbox
        return orderSagaManager.newOrder(orderInput, userInfoDTO)
    }

    override fun updateOrderStatus(orderUpdateInput: OrderUpdateInput, userInfo: UserInfoDTO): UpdateResult
    {
        //Only admins can update the status of an order
        if (!userInfo.roles.contains("ADMIN"))
            return UpdateResult(false, "Request denied due to lack of privileges")

        val order = orderRepository.findByIdOrNull(orderUpdateInput.orderId) ?: return UpdateResult(false, "Order not found")

        //Check if the order has a status that permits to update the order status
        if (order.orderStatus == OrderStatus.CANCELED || order.orderStatus == OrderStatus.FAILED)
            return UpdateResult(false, "The status of the order cannot be changed")
        if (order.orderStatus == orderUpdateInput.orderStatus)
            return UpdateResult(false, "The provided status is equal to the current one")

        //Update the status
        order.orderStatus = orderUpdateInput.orderStatus
        orderRepository.save(order)

        //Notify via Kafka to "Catalog" to send an email to the customer who made this order
        val emailMessage = EmailEvent(order.orderId!!, order.buyerId, order.orderStatus)
        kafkaTemplate.send(TO_CATALOG_TOPIC, emailMessage.toString())
        println("email")

        return UpdateResult(true)
    }

    override fun deleteOrderById(orderId: Long, userInfo: UserInfoDTO): DeletionResult
    {
        val order = orderRepository.findByIdOrNull(orderId) ?: return DeletionResult(false, "Order not found")

        //Only an admin can delete an order of another user
        if (order.buyerId != userInfo.username && !userInfo.roles.contains("ADMIN"))
            return DeletionResult(false, "Request denied due to lack of privileges")
        //Check if the order is in a status that permits to cancel it
        if (order.orderStatus == OrderStatus.CANCELED)
            return DeletionResult(false, "Order has already been deleted previously")
        if (!order.canBeDeleted())
            return DeletionResult(false, "It's not possible to cancel order anymore")

        //Delete an existing order using patterns Saga and Outbox
        orderSagaManager.deleteOrder(order)

        return DeletionResult(true)
    }

    override fun getAllOrders(userInfoDTO: UserInfoDTO, selectionSet: DataFetchingFieldSelectionSet): List<OrderDTO>
    {
        //Only admins can get the global list of orders
        if (!userInfoDTO.roles.contains("ADMIN"))
            throw ForbiddenException("Request denied due to lack of privileges")
        //Get the global list (because I'm passing an empty username)
        return this.getOrders("", selectionSet)
    }

    override fun getOrdersByUser(username: String, selectionSet: DataFetchingFieldSelectionSet): List<OrderDTO>
    {
        //Get the list of order belonging to a given user (identified by its username)
        return this.getOrders(username, selectionSet)
    }

    override fun getOrderById(orderId: Long, userInfoDTO: UserInfoDTO, selectionSet: DataFetchingFieldSelectionSet): OrderDTO?
    {
        //Try to get order
        val order = orderRepository.findByIdOrNull(orderId) ?: return null

        //Only an admin can get orders of another user
        if (order.buyerId != userInfoDTO.username && !userInfoDTO.roles.contains("ADMIN"))
            throw ForbiddenException("Request denied due to lack of privileges")

        //If the products list was required, fill the retrieved order with the products information.
        val orderDto = order.toOrderDTO()
        if (selectionSet.contains("productsList"))
            orderDto.productsList = purchasedProductRepository.findAllByOrderId(orderId).map { it.toPurchasedProductDTO() }

        return orderDto
    }

    private fun getOrders(username: String, selectionSet: DataFetchingFieldSelectionSet): List<OrderDTO>
    {
        val orders = if (username.isEmpty())
        {
            //Get all the orders
            orderRepository.findAll().map { it.toOrderDTO() }
        }
        else
        {
            //Get all the orders performed by the user who made the request
            orderRepository.findAllByBuyerId(username).map { it.toOrderDTO() }
        }

        if (orders.count() >= 1) //Check if user has done at least one order
        {
            if (selectionSet.contains("productsList"))
            {
                //As output, the user has requested also the list of products associated to the order
                orders.forEach { order ->
                    order.productsList = purchasedProductRepository.findAllByOrderId(order.orderId).map { it.toPurchasedProductDTO() }
                }
            }
            if (selectionSet.contains("delivery"))
            {
                //As output, the user has requested also the delivery address associated to the order
                orders.forEach { order ->
                    order.delivery = deliveryRepository.findByOrderId(order.orderId)?.toDeliveryDTO()
                }
            }
        }
        return orders
    }
}
