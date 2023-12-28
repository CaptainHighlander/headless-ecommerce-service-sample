package it.polito.wa2group8.warehouse_service.saga_outbox

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class DebeziumConnectorConfig
{
    private val connectorJavaClassName = "io.debezium.connector.mysql.MySqlConnector"
    @Value("\${spring.datasource.debezium-connector-name}")
    private val connectorName = ""
    @Value("\${spring.datasource.debezium-cluster-name}")
    private val clusterName = ""
    @Value("\${spring.datasource.debezium-id}")
    private val id = -1
    @Value("\${spring.datasource.database-host}")
    private val warehouseDbHost: String = ""
    @Value("\${spring.datasource.database-name}")
    private val warehouseDbName: String = ""
    @Value("\${spring.datasource.database-port}")
    private val warehouseDbPort: String = ""
    @Value("\${spring.datasource.username}")
    private val warehouseDbUsername: String = ""
    @Value("\${spring.datasource.password}")
    private val warehouseDbPassword: String = ""
    @Value("\${spring.datasource.outbox-table}")
    private val outboxTable: String = ""

    @Bean
    fun customerConnector(): io.debezium.config.Configuration
    {
        val offsetStorageTempFile: File = File.createTempFile("offsets_", ".dat")
        val dbHistoryTempFile: File = File.createTempFile("dbhistory_", ".dat")
        return io.debezium.config.Configuration.create()
            .with("name", connectorName)
            .with("connector.class", connectorJavaClassName)
            .with("database.hostname", warehouseDbHost)
            .with("database.port", warehouseDbPort)
            .with("database.user", warehouseDbUsername)
            .with("database.password", warehouseDbPassword)
            .with("database.dbname", warehouseDbName)
            .with("database.server.name", clusterName)
            .with("database.server.id", "$id")
            .with("database.include.list", warehouseDbName) //List of databases to monitor by Debezium connector
            .with("table.include.list", "$warehouseDbName.$outboxTable") //List of tables to monitor by Debezium connector
            .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
            .with("offset.storage.file.filename", offsetStorageTempFile.absolutePath)
            .with("offset.flush.interval.ms", "60000")
            .with("include.schema.changes", "false")
            .with("database.allowPublicKeyRetrieval", "true")
            .with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
            .with("database.history.file.filename", dbHistoryTempFile.absolutePath)
            .build()
    }
}
