package it.polito.wa2group8.order_service.security

import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContext
import io.jsonwebtoken.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.security.Key
import java.util.*
import io.jsonwebtoken.security.Keys
import it.polito.wa2group8.order_service.dto.UserInfoDTO
import it.polito.wa2group8.order_service.exceptions.BadRequestException
import it.polito.wa2group8.order_service.exceptions.ForbiddenException
import javax.annotation.PostConstruct

@Configuration
class JwtUtils()
{
    @Value("\${security.jwt.jwtSecret}")
    private val jwtSecret: String = ""

    private lateinit var key: Key
    @PostConstruct
    private fun init()
    {
        key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun getTokenFromHeader(context: SpringGraphQLContext): String
    {
        //Get "Authorization" header
        val authHeader = context.getHTTPRequestHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw BadRequestException("Invalid Auth header")
        //Get the JWT from the "Authorization" header and return it
        return authHeader.replace("Bearer ", "")
    }

    fun getDetailsFromJwtToken(authToken: String): UserInfoDTO
    {
        //Check JWT validity
        if (!isJwtTokenValid(authToken))
            throw ForbiddenException("Invalid JWT")

        //Get address from token payload
        val claimsFromToken = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken).body
        val address: String = claimsFromToken["delivery_address"] as String
        val addressInfo = address.split('\n')

        //Get roles form token payload
        @Suppress("UNCHECKED_CAST") //N.B.: I assume that Catalog microservice has done things correctly and none can modify the payload
        val roles = claimsFromToken["role"] as ArrayList<String>

        //Produce a proper DTO from extracted claims
        return UserInfoDTO(claimsFromToken.subject, addressInfo[0], addressInfo[1], addressInfo[2], roles)
    }

    fun isJwtTokenValid(authToken: String): Boolean
    {
        return try {
            !isTokenExpired(authToken)
        }
        catch(ex: Exception) {
            false
        }
    }

    private fun isTokenExpired(authToken: String): Boolean
    {
        return try {
            val claimsFromToken = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken).body
            val expiration: Date = claimsFromToken.expiration
            expiration.before(Date())
        }
        catch (ex: Exception) {
            true
        }
    }
}
