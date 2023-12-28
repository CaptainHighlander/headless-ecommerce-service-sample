package it.polito.wa2group8.catalog_service.security

import io.jsonwebtoken.*
import it.polito.wa2group8.catalog_service.dto.UserDetailsDTO
import it.polito.wa2group8.catalog_service.exceptions.NotFoundException
import it.polito.wa2group8.catalog_service.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.security.Key
import java.security.SignatureException
import java.util.*
import io.jsonwebtoken.security.Keys
import it.polito.wa2group8.catalog_service.exceptions.BadRequestException
import it.polito.wa2group8.catalog_service.exceptions.ForbiddenException
import kotlinx.coroutines.runBlocking
import java.util.HashMap
import javax.annotation.PostConstruct

@Configuration
class JwtUtils(val userRepository: UserRepository)
{
    @Value("\${security.jwt.jwtSecret}")
    private val jwtSecret: String = ""

    @Value("\${security.jwt.jwtExpirationMs}")
    private val jwtExpirationMs: Int = 0

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

    fun getDetailsFromJwtToken(authToken: String): UserDetailsDTO
    {
        //Check JWT validity
        if (!isJwtTokenValid(authToken))
            throw ForbiddenException("Invalid JWT")

        //Get information from token payload
        val claimsFromToken = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken).body
        // Get UserDetails from DB and produce a corresponding DTO
        val user = runBlocking { userRepository.findUserByUsername(claimsFromToken.subject) } ?: throw NotFoundException("User not found")
        return UserDetailsDTO(user.username, null, null, null, user.getRolenames(), user.getDeliveryAddress())
    }

    fun getUserFromJwtToken(authToken: String): String
    {
        //Check JWT validity
        if (!isJwtTokenValid(authToken))
            throw ForbiddenException("Invalid JWT")

        //Get information from token payload
        val claimsFromToken = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken).body

        return claimsFromToken.subject
    }

    fun generateJwtToken(userDTO: UserDetailsDTO): String
    {
        //Set user's claims
        val claims: MutableMap<String, Any> = HashMap()
        claims["role"] = userDTO.getRoles()
        claims["delivery_address"] = userDTO.getDeliveryAddress() //N.B.: This is used by order microservice

        //Time info about token
        val createdDate = Date()
        val expirationDate = Date(createdDate.time + jwtExpirationMs)

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDTO.username)
            .setIssuedAt(createdDate)
            .setExpiration(expirationDate)
            .signWith(key)
            .compact()
    }

    fun isJwtTokenValid(authToken: String): Boolean
    {
        try
        {
            return !isTokenExpired(authToken)
        }
        catch (ex: SignatureException) {
            return false
        }
        catch (ex: MalformedJwtException ) {
            return false
        }
        catch (ex: ExpiredJwtException) {
            return false
        }
        catch (ex: UnsupportedJwtException) {
            return false
        }
        catch (ex: IllegalArgumentException) {
            return false
        }
        catch(ex: Exception) {
            return false
        }
    }

    private fun isTokenExpired(authToken: String): Boolean
    {
        try {
            val claimsFromToken = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken).body
            val expiration: Date = claimsFromToken.expiration
            return expiration.before(Date())
        }
        catch (ex: SignatureException) {
            return true
        }
        catch (ex: MalformedJwtException ) {
            return true
        }
        catch (ex: ExpiredJwtException) {
            return true
        }
        catch (ex: UnsupportedJwtException) {
            return true
        }
        catch (ex: IllegalArgumentException) {
            return true
        }
        catch (ex: Exception) {
            return true
        }
    }
}
