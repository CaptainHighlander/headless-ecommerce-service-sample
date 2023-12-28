package it.polito.wa2group8.warehouse_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@EnableEurekaClient
@SpringBootApplication
class WarehouseServiceApplication

fun main(args: Array<String>)
{
    runApplication<WarehouseServiceApplication>(*args)
}
