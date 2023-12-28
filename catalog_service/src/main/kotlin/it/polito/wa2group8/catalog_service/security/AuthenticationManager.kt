package it.polito.wa2group8.catalog_service.security

import it.polito.wa2group8.catalog_service.dto.UserDetailsDTO
import it.polito.wa2group8.catalog_service.exceptions.AccountNotEnabledException
import it.polito.wa2group8.catalog_service.exceptions.BadCredentialsException
import java.util.stream.Collectors
import it.polito.wa2group8.catalog_service.services.UserDetailsService
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Mono
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

const val PATH_LOGIN: String = "/auth/login"

@Component
class AuthenticationManager(val jwtUtils: JwtUtils, val userDetailsService: UserDetailsService): ReactiveAuthenticationManager
{
    private val passwordEncoder = BCryptPasswordEncoder()

    private fun isAuthenticatedUserEnabled(username: String): Boolean
    {
        val user = runBlocking { userDetailsService.findByUsername(username) }
        if (!user.isEnabled) throw AccountNotEnabledException("Your account is disabled")
        return true
    }

    override fun authenticate(authentication: Authentication): Mono<Authentication>
    {
        val authToken: String = authentication.credentials.toString()
        val username: String = jwtUtils.getDetailsFromJwtToken(authToken).username
        return Mono.just(jwtUtils.isJwtTokenValid(authToken))
            .filter{ valid -> valid }
            .switchIfEmpty(Mono.empty())
            .map {
                isAuthenticatedUserEnabled(username)
                val details = jwtUtils.getDetailsFromJwtToken(authToken)
                val rolesMap = details.getRoles()
                //Set security context
                val securityContext = UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    rolesMap.stream()
                        .map { role: String? -> SimpleGrantedAuthority(role) }
                        .collect(Collectors.toList())
                )
                SecurityContextHolder.getContext().authentication = securityContext
                //Return value
                securityContext
            }
    }

    @Transactional
    suspend fun areCredentialsValid(username: String, password: String): UserDetailsDTO
    {
        val user = userDetailsService.findByUsername(username)
        //Compare provided password with stored hash
        if (!passwordEncoder.matches(password, user.password))
            throw BadCredentialsException("Invalid Credentials")
        return user
    }
}
