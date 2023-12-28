package it.polito.wa2group8.warehouse_service.services

import it.polito.wa2group8.warehouse_service.domain.Product
import it.polito.wa2group8.warehouse_service.dto.*
import it.polito.wa2group8.warehouse_service.exceptions.BadRequestException
import it.polito.wa2group8.warehouse_service.exceptions.NotFoundException
import it.polito.wa2group8.warehouse_service.repositories.CommentRepository
import it.polito.wa2group8.warehouse_service.repositories.ProductRepository
import it.polito.wa2group8.warehouse_service.repositories.ProductWarehouseRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val productWarehouseRepository: ProductWarehouseRepository,
    private val commentRepository: CommentRepository,
): ProductService
{
    override fun getProducts(category: String?): List<ProductDTO>
    {
        return if (category == null)
            productRepository.findAll().map{ it.toProductDTO() }
        else
            productRepository.findByCategory(category).map{ it.toProductDTO() }
    }

    override fun getProduct(productID: Long): ProductDTO?
    {
        val product = productRepository.findByIdOrNull(productID) ?: throw NotFoundException("Product not found")
        return product.toProductDTO()
    }

    override fun createOrUpdateProduct(productID: Long?, productDTO: ProductDTO): ProductDTO?
    {
        //pictureURL nullable?
        if (productDTO.name == null || productDTO.description == null || productDTO.category == null || productDTO.price == null)
            throw BadRequestException("Fields cannot be null")

        return try
        {
            //Try to update an existing product (full representation thanks to previous checks)
            updateProduct(productID ?: -1, productDTO)
        }
        catch (e: NotFoundException)
        {
            val product = Product(null, productDTO.name , productDTO.description, productDTO.category, productDTO.price)
            val createdProduct = productRepository.save(product)
            createdProduct.toProductDTO()
        }
    }

    override fun updateProduct(productID: Long, productDTO: ProductDTO): ProductDTO
    {
        val product = productRepository.findByIdOrNull(productID) ?: throw NotFoundException("Product not found")
        product.name = productDTO.name ?: product.name
        product.description = productDTO.description ?: product.description
        product.category = productDTO.category ?: product.category
        product.price = productDTO.price ?: product.price
        val updatedProduct = productRepository.save(product)
        return updatedProduct.toProductDTO()
    }

    override fun deleteProduct(productID: Long)
    {
        productRepository.findByIdOrNull(productID) ?: throw NotFoundException("Product not found")
        //Delete comments associated to the product
        commentRepository.deleteAllByProductId(productID)
        //Delete product warehouse associated to the product
        productWarehouseRepository.deleteAllByProductId(productID)
        //LASTLY (in order to don't violate UKs constraints), delete product
        productRepository.deleteById(productID)
    }

    override fun getProductWarehouses(productID: Long): List<WarehouseDTO>
    {
        val product = productRepository.findByIdOrNull(productID) ?: throw NotFoundException("Product not found")
        return productWarehouseRepository.findAllByProduct(product).map { it.warehouse.toWarehouseDTO() }
    }

    override fun addComment(productID: Long, commentDTO: CommentDTO)
    {
        val product = productRepository.findByIdOrNull(productID) ?: throw NotFoundException("Product not found")
        //Update product
        product.updateAverageRating(commentDTO.stars)
        productRepository.updateCommentsNoAndAverageRating(product.commentsNumber, product.averageRating, productID)
        //Add a comment
        val newComment = commentDTO.toCommentEntity(product)
        commentRepository.save(newComment)
    }

    override fun getComments(productID: Long): List<CommentDTO>
    {
        val product = productRepository.findByIdOrNull(productID) ?: throw NotFoundException("Product not found")
        return product.comments.map{ it.toCommentDTO() }
    }

    override fun setProductPicture(productID: Long, file: MultipartFile)
    {
        val product = productRepository.findByIdOrNull(productID) ?: throw NotFoundException("Product not found")
        product.picture = file.bytes
        productRepository.save(product)
    }

    override fun getProductPicture(productID: Long): ByteArray
    {
        val product = productRepository.findByIdOrNull(productID) ?: throw NotFoundException("Product not found")
        return product.picture ?: throw NotFoundException("Product doesn't have any picture yet")
    }

    override fun getPictureAndPictureURL(productID: Long): Pair<ByteArray, String>
    {
        val product = productRepository.findByIdOrNull(productID) ?: throw NotFoundException("Product not found")
        val image = product.picture ?: throw NotFoundException("Product doesn't have any picture yet")
        return Pair(image, product.pictureURL)
    }
}
