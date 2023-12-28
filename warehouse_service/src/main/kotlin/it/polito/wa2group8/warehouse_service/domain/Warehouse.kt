package it.polito.wa2group8.warehouse_service.domain

import it.polito.wa2group8.warehouse_service.dto.WarehouseDTO
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

@Entity
class Warehouse (
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "warehouse_id")
    var warehouseId: Long?,

    @get:NotEmpty
    @get:NotBlank
    var name: String,

    @get:NotEmpty
    @get:NotBlank
    var location: String,
)
{
    @OneToMany(mappedBy = "warehouse", targetEntity = ProductWarehouse::class, fetch = FetchType.LAZY)
    var products: MutableSet<ProductWarehouse> = mutableSetOf()

    fun toWarehouseDTO() = WarehouseDTO(warehouseId, name, location)
}
