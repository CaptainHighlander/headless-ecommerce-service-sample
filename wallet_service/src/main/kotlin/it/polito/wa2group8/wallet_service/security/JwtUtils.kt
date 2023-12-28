package it.polito.wa2group8.wallet_service.security


import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.wa2group8.wallet_service.dto.UserInfoDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.security.Key
import java.util.*
import it.polito.wa2group8.wallet_service.exceptions.BadRequestException
import it.polito.wa2group8.wallet_service.exceptions.ForbiddenException
import javax.annotation.PostConstruct
import kotlin.collections.ArrayList

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

    fun getTokenFromHeader(authHeader: String): String
    {
        //Get "Authorization" header
        if (!authHeader.startsWith("Bearer "))
            throw BadRequestException("Invalid Auth header")
        //Get the JWT from the "Authorization" header and return it
        return authHeader.replace("Bearer ", "")
    }

    fun getDetailsFromJwtToken(authToken: String): UserInfoDTO
    {
        //Check JWT validity
        if (!isJwtTokenValid(authToken))
            throw ForbiddenException("Invalid JWT")

        //Get information from token payload
        val claimsFromToken = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken).body
        println(claimsFromToken)
        //Produce a proper DTO from extracted claims
        return UserInfoDTO(claimsFromToken.subject, claimsFromToken["role"] as ArrayList<String>)
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
