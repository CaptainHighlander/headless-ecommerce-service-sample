package it.polito.wa2group8.warehouse_service.domain

import it.polito.wa2group8.warehouse_service.dto.ProductDTO
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.util.*
import javax.persistence.*
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

@Entity
class Product(
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "product_id")
    var productId: Long?,

    name: String,

    @get:NotEmpty
    @get:NotBlank
    var description: String,

    @get:NotEmpty
    @get:NotBlank
    var category: String,

    @get:DecimalMin(value = "0.0", message = "The value must be a positive or zero value", inclusive = true)
    var price: BigDecimal,
)
{
    var name: String = name
        set(value)
        {
            field = value
            pictureURL = "picture_$value"
        }

    var pictureURL: String = "picture_$name"

    @Lob //Lob indicates that this column stores a BLOB (Binary Large Object)
    var picture: ByteArray? = null

    @get:DecimalMin(value = "0.0", message = "The value must be a positive or zero value", inclusive = true)
    var averageRating: BigDecimal = BigDecimal("0.0")

    @get:DateTimeFormat
    var creationDate: Date = Date()

    @OneToMany(mappedBy = "product", targetEntity = ProductWarehouse::class, fetch = FetchType.LAZY)
    var warehouses: MutableSet<ProductWarehouse> = mutableSetOf()

    @OneToMany(mappedBy = "product", targetEntity = Comment::class, fetch = FetchType.LAZY)
    var comments: MutableSet<Comment> = mutableSetOf()

    @get:Min(value = 0, message = "Comments number cannot be lower than 0")
    var commentsNumber: Int = 0

    fun toProductDTO() = ProductDTO(productId, name, description, category, price, averageRating, creationDate)

    fun updateAverageRating(newStars: Int)
    {
        val currentTot = averageRating * commentsNumber.toBigDecimal()
        commentsNumber += 1
        averageRating = (currentTot + newStars.toBigDecimal()) / commentsNumber.toBigDecimal()
    }
}
