package it.polito.wa2group8.catalog_service

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator


@SpringBootApplication
@EnableEurekaClient
class CatalogServiceApplication
{
    @Bean
    fun initializer(@Qualifier("connectionFactory") connectionFactory: ConnectionFactory): ConnectionFactoryInitializer
    {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)

        val populator = CompositeDatabasePopulator();
        //Populator for schema
        populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("schema.sql")))
        //Populator for data
        populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("data.sql")))
        //Set populator
        initializer.setDatabasePopulator(populator)
        // This will drop our table after we are done so we can have a fresh start next run
        //initializer.setDatabaseCleaner(ResourceDatabasePopulator(ClassPathResource("cleanup.sql")))
        return initializer
    }
}

fun main(args: Array<String>)
{
    runApplication<CatalogServiceApplication>(*args)
}
