package it.polito.wa2group8.order_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@EnableEurekaClient
@SpringBootApplication
class OrderServiceApplication

fun main(args: Array<String>)
{
    runApplication<OrderServiceApplication>(*args)
}
