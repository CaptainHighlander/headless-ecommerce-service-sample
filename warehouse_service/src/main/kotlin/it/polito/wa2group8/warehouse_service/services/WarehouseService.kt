package it.polito.wa2group8.warehouse_service.services

import it.polito.wa2group8.warehouse_service.dto.WarehouseDTO
import it.polito.wa2group8.warehouse_service.dto.WarehouseProductsDTO

interface WarehouseService
{
    /**
     * Retrieves the list of all warehouses
     */
    fun getWarehouses(): List<WarehouseDTO>

    /**
     * Retrieves the warehouse identified by warehouseID
     */
    fun getWarehouseById(warehouseID: Long): WarehouseDTO?

    /**
     * Updates an existing warehouse (full representation) or adds a new one if not exists
     */
    fun createOrUpdateWarehouse(warehouseID: Long?, warehouseDTO: WarehouseDTO): WarehouseDTO?

    /**
     * Updates an existing warehouse (partial representation)
     */
    fun updateWarehouse(warehouseID: Long, warehouseDTO: WarehouseDTO): WarehouseDTO?

    /**
     * Deletes the warehouse identified by warehouseID
     */
    fun deleteWarehouse(warehouseID: Long)

    /**
     * Adds some products to the warehouse identified by warehouseID
     */
    fun addProducts(warehouseID: Long, warehouseProductsDTO: WarehouseProductsDTO): List<Long>
}
