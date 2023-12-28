package it.polito.wa2group8.catalog_service.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException

import reactor.core.publisher.Mono

import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.AuthenticationException

import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.server.ServerWebExchange


@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfig(private val authenticationManager: AuthenticationManager, private val securityContextRepository: SecurityContextRepository)
{
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain
    {
        return http
            .exceptionHandling()
            .authenticationEntryPoint { swe: ServerWebExchange, _: AuthenticationException? ->
                Mono.fromRunnable { swe.response.statusCode = HttpStatus.UNAUTHORIZED }
            }.accessDeniedHandler { swe: ServerWebExchange, _: AccessDeniedException? ->
                Mono.fromRunnable { swe.response.statusCode = HttpStatus.FORBIDDEN }
            }.and()
            .cors().and().csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .authorizeExchange()
            .pathMatchers(HttpMethod.OPTIONS).permitAll()
            .pathMatchers("/auth/**").permitAll()
            .pathMatchers("/").permitAll()
            .pathMatchers("/graphql").hasAnyAuthority("CUSTOMER", "ADMIN") //Endpoint towards Order microservice
            .pathMatchers("/playground").permitAll() //Just for testing graphQL without AA
            .pathMatchers("/wallets/{walletID}/transactions/**").hasAnyAuthority("ADMIN")
            .pathMatchers("/wallets/**").hasAnyAuthority("CUSTOMER", "ADMIN")
            .pathMatchers(HttpMethod.GET,"/products/**").permitAll()
            .pathMatchers(HttpMethod.GET,"products/{productID}/comments").permitAll()
            .pathMatchers(HttpMethod.POST,"products/{productID}/comments").hasAnyAuthority("CUSTOMER", "ADMIN")
            .pathMatchers(HttpMethod.POST,"/products/**").hasAnyAuthority("ADMIN")
            .pathMatchers(HttpMethod.PUT,"/products/**").hasAnyAuthority("ADMIN")
            .pathMatchers(HttpMethod.PATCH,"/products/**").hasAnyAuthority("ADMIN")
            .pathMatchers(HttpMethod.DELETE,"/products/**").hasAnyAuthority("ADMIN")
            .pathMatchers("/warehouses/**").hasAnyAuthority("ADMIN")
            .anyExchange().authenticated()
            .and().build()
    }
}
