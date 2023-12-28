package it.polito.wa2group8.warehouse_service.controllers

import it.polito.wa2group8.warehouse_service.dto.WarehouseDTO
import it.polito.wa2group8.warehouse_service.dto.WarehouseProductsDTO
import it.polito.wa2group8.warehouse_service.services.WarehouseService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
class WarehouseController(private val warehouseService: WarehouseService)
{
    @GetMapping(value = ["/warehouses"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getWarehouses(): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(warehouseService.getWarehouses())
    }

    @GetMapping(value = ["/warehouses/{warehouseID}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getWarehouseById(
        @PathVariable warehouseID: Long
    ): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(warehouseService.getWarehouseById(warehouseID))
    }

    @PostMapping(value = ["/warehouses"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createWarehouse(@RequestBody warehouseDTO: WarehouseDTO): ResponseEntity<Any>
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.createOrUpdateWarehouse(null, warehouseDTO))
    }

    @PutMapping(value = ["/warehouses/{warehouseID}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createOrUpdateWarehouse(@PathVariable warehouseID: Long, @RequestBody warehouseDTO: WarehouseDTO): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(warehouseService.createOrUpdateWarehouse(warehouseID, warehouseDTO))
    }

    @PatchMapping(value = ["/warehouses/{warehouseID}"], produces=[MediaType.APPLICATION_JSON_VALUE])
    fun updateWarehouse(@PathVariable warehouseID: Long, @RequestBody warehouseDTO: WarehouseDTO): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(warehouseService.updateWarehouse(warehouseID, warehouseDTO))
    }

    @DeleteMapping(value = ["/warehouses/{warehouseID}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deleteWarehouse(@PathVariable warehouseID: Long): ResponseEntity<Any>
    {
        warehouseService.deleteWarehouse(warehouseID)
        return ResponseEntity.noContent().build()
    }

    @PutMapping(value = ["/warehouses/{warehouseID}/products"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun addProductsToWarehouse(
        @PathVariable warehouseID: Long,
        @RequestBody @Valid warehouseProductsDTO: WarehouseProductsDTO,
    ): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(warehouseService.addProducts(warehouseID, warehouseProductsDTO))
    }
}
