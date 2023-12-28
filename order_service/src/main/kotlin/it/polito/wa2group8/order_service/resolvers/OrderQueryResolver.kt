package it.polito.wa2group8.order_service.resolvers

import graphql.schema.DataFetchingEnvironment

import com.expediagroup.graphql.server.operations.Query
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContext
import it.polito.wa2group8.order_service.dto.OrderDTO
import it.polito.wa2group8.order_service.dto.UserInfoDTO
import it.polito.wa2group8.order_service.security.JwtUtils
import it.polito.wa2group8.order_service.services.OrderService
import org.springframework.stereotype.Component

@Component
class OrderQueryResolver(private val jwtUtils: JwtUtils, private val orderService: OrderService): Query
{
    @Suppress("UNUSED")
    /**
     * Retrieves all the orders.
     * It's reserved to admins.
     */
    fun allOrders(environment: DataFetchingEnvironment): List<OrderDTO>
    {
        val userInfo = getUserInfoByJwt(environment)
        return orderService.getAllOrders(userInfo, environment.selectionSet)
    }

    @Suppress("UNUSED")
    /**
     * Retrieves the list of all orders (of a specific user)
     */
    fun orders(environment: DataFetchingEnvironment): List<OrderDTO>
    {
        val userInfo = getUserInfoByJwt(environment)
        return orderService.getOrdersByUser(userInfo.username, environment.selectionSet)
    }

    /**
     * Retrieves the order identified by orderID
     */
    fun order(orderId: Long, environment: DataFetchingEnvironment): OrderDTO?
    {
        val userInfo = getUserInfoByJwt(environment)
        return orderService.getOrderById(orderId, userInfo, environment.selectionSet)
    }

    private fun getUserInfoByJwt(environment: DataFetchingEnvironment): UserInfoDTO
    {
        //Get context
        val context: SpringGraphQLContext = environment.getContext()

        //Extract JWT from request
        val jwt = jwtUtils.getTokenFromHeader(context)

        //Get and return information about the user from the JWT
        return jwtUtils.getDetailsFromJwtToken(jwt)
    }
}
