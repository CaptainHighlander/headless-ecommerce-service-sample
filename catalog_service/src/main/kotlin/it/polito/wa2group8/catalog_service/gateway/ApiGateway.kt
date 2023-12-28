package it.polito.wa2group8.catalog_service.gateway

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@Configuration
@RestController
class ApiGateway
{
    @Bean
    fun defaultCustomizer(): Customizer<ReactiveResilience4JCircuitBreakerFactory>
    {
        return Customizer { factory ->
            factory.configureDefault { id ->
                Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                    .timeLimiterConfig(
                        TimeLimiterConfig
                            .custom()
                            .timeoutDuration(
                                Duration.ofSeconds(4)
                            ).build()
                    )
                    .build()
            }
        }
    }

    @Bean
    fun gatewayRoutes(builder: RouteLocatorBuilder): RouteLocator
    {
        return builder.routes()
            .route("graphql") { it ->
                it.path("/graphql/**")
                    .filters {
                        f -> f.circuitBreaker {
                            it.setFallbackUri("forward:/orderFailure")
                        }
                    }
                    .uri("lb://order") //Order microservice
            }
            .route("graphql-playground") { it ->
                it.path("/playground")
                    .filters {
                        f -> f.circuitBreaker {
                            it.setFallbackUri("forward:/orderFailure")
                        }
                    }
                    .uri("lb://order") //Order microservice
            }
            .route("wallet") { it ->
                it.path("/wallets/**")
                    .filters {
                        f -> f.circuitBreaker {
                            it.setFallbackUri("forward:/walletFailure")
                        }
                    }
                    .uri("lb://wallet") //Wallet microservice
            }
            .route("product") { it ->
                it.path("/products/**")
                    .filters {
                        f -> f.circuitBreaker {
                            it.setFallbackUri("forward:/warehouseFailure")
                        }
                    }
                    .uri("lb://warehouse") // Warehouse microservice
            }
            .route("warehouse") { it ->
                it.path("/warehouses/**")
                    .filters {
                        f -> f.circuitBreaker {
                            it.setFallbackUri("forward:/warehouseFailure")
                        }
                    }
                    .uri("lb://warehouse") // Warehouse microservice
            }
            .build()
    }

    @GetMapping("/orderFailure")
    fun orderServiceFailure(): String
    {
        return "Order Service is unavailable, try again later."
    }

    @GetMapping("/walletFailure")
    fun walletServiceFailure(): String
    {
        return "Wallet Service is unavailable, try again later."
    }

    @GetMapping("/warehouseFailure")
    fun warehouseServiceFailure(): String
    {
        return "Warehouse Service is unavailable, try again later."
    }
}
