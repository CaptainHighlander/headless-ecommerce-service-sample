package it.polito.wa2group8.order_service.resolvers

import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContext
import graphql.schema.DataFetchingEnvironment
import it.polito.wa2group8.order_service.dto.*
import it.polito.wa2group8.order_service.security.JwtUtils
import it.polito.wa2group8.order_service.services.OrderService
import org.springframework.stereotype.Component


@Component
class OrderMutationResolver(private val jwtUtils: JwtUtils, private val orderService: OrderService): Mutation
{
    @Suppress("UNUSED")
    /**
     * Adds a new order (for a given user)
     */
    fun newOrder(orderInput: OrderInput, environment: DataFetchingEnvironment): Long?
    {
        val userInfo = getUserInfoByJwt(environment)
        return orderService.createOrder(orderInput, userInfo)
    }

    @Suppress("UNUSED")
    /**
     * Updates the status of the order identified by orderId.
     * It's reserved to admins.
     */
    fun updateOrder(orderUpdateInput: OrderUpdateInput, environment: DataFetchingEnvironment): UpdateResult
    {
        val userInfo = getUserInfoByJwt(environment)
        return orderService.updateOrderStatus(orderUpdateInput, userInfo)
    }

    @Suppress("UNUSED")
    /**
     * Cancels an existing order, if possible
     */
    fun deleteOrder(orderId: Long, environment: DataFetchingEnvironment): DeletionResult
    {
        val userInfo = getUserInfoByJwt(environment)
        return orderService.deleteOrderById(orderId, userInfo)
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
