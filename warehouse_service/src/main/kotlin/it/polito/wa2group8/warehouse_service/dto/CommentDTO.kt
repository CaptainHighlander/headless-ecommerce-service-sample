package it.polito.wa2group8.warehouse_service.dto

import it.polito.wa2group8.warehouse_service.domain.Comment
import it.polito.wa2group8.warehouse_service.domain.Product
import org.springframework.format.annotation.DateTimeFormat
import java.util.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class CommentDTO (
    @get:NotEmpty(message = "Empty title")
    @get:NotBlank(message = "Blank title")
    var title: String,

    @get:NotEmpty(message = "Empty body")
    @get:NotBlank(message = "Blank body")
    var body: String,

    @get:Min(value = 0, message = "Stars cannot be negative")
    @get:Max(value = 5, message = "Stars cannot be more than five")
    var stars: Int,

    @get:DateTimeFormat()
    var creationDate: Date,
)

fun CommentDTO.toCommentEntity(product: Product) = Comment(null, title, body, stars, creationDate, product)

fun Comment.toCommentDTO(): CommentDTO = CommentDTO(title, body, stars, creationDate)
