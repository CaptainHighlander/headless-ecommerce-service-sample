package it.polito.wa2group8.catalog_service.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


@Component
class SecurityContextRepository(private val authenticationManager: AuthenticationManager): ServerSecurityContextRepository
{
    override fun save(swe: ServerWebExchange, sc: SecurityContext): Mono<Void>
    {
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun load(swe: ServerWebExchange): Mono<SecurityContext>
    {
        return Mono.justOrEmpty(swe.request.headers.getFirst(HttpHeaders.AUTHORIZATION)) //Get first Authorization header in the request
            .filter { authHeader: String -> authHeader.startsWith("Bearer ") } //Keep only Authorization headers starting with Bearer
            .flatMap { authHeader: String ->
                val authToken = authHeader.substring(7) //Remove prefix "Bearer"
                val auth = UsernamePasswordAuthenticationToken(authToken, authToken)
                //Authenticate the request
                authenticationManager.authenticate(auth)
                    .map { authentication: Authentication? -> SecurityContextImpl(authentication) }
            }
    }
}
