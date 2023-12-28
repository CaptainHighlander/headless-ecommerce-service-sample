package it.polito.wa2group8.warehouse_service.controllers

import it.polito.wa2group8.warehouse_service.dto.CommentDTO
import it.polito.wa2group8.warehouse_service.dto.ProductDTO
import it.polito.wa2group8.warehouse_service.services.ProductService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@RestController
class ProductController(private val productService: ProductService)
{
    @GetMapping(value = ["/products"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProducts(@RequestParam("category", required = false) category: String?): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(productService.getProducts(category))
    }

    @GetMapping(value = ["/products/{productID}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProductByID(@PathVariable productID: Long): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(productService.getProduct(productID))
    }

    @PostMapping(value = ["/products"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createProduct(@RequestBody @Valid product: ProductDTO): ResponseEntity<Any>
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createOrUpdateProduct(null, product))
    }

    @PutMapping(value = ["/products/{productID}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateOrCreateProduct(@PathVariable productID: Long, @RequestBody @Valid product: ProductDTO): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(productService.createOrUpdateProduct(productID, product))
    }

    @PatchMapping(value = ["/products/{productID}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateProduct(@PathVariable productID: Long, @RequestBody @Valid product: ProductDTO): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(productService.updateProduct(productID, product))
    }

    @DeleteMapping(value = ["/products/{productID}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun deleteProduct(@PathVariable productID: Long): ResponseEntity<Any>
    {
        productService.deleteProduct(productID)
        return ResponseEntity.noContent().build()
    }

    @GetMapping(value = ["/products/{productID}/warehouses"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProductWarehouses(@PathVariable productID: Long): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(productService.getProductWarehouses(productID))
    }

    @PostMapping(value = ["products/{productID}/comments"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun addCommentToProduct(
        @PathVariable("productID") productID: Long,
        @RequestBody @Valid commentDTO: CommentDTO,
        bindingResult: BindingResult
    ): ResponseEntity<Any>
    {
        if (bindingResult.hasErrors()) return ResponseEntity.badRequest().body(bindingResult.fieldErrors)
        productService.addComment(productID, commentDTO)
        return ResponseEntity.status(HttpStatus.CREATED).body("")
    }

    @GetMapping(value = ["/products/{productID}/comments"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProductComments(@PathVariable productID: Long): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(productService.getComments(productID))
    }

    @PostMapping(value = ["/products/{productID}/pictures"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseBody
    fun addProductPicture(@PathVariable productID: Long, @RequestParam("image") image: MultipartFile): ResponseEntity<Any>
    {
        productService.setProductPicture(productID, image)
        return ResponseEntity.status(HttpStatus.CREATED).body("")
    }

    @GetMapping(value = ["/products/{productID}/pictures"], produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getProductPicture(@PathVariable productID: Long): ResponseEntity<Any>
    {
        val pictureInfo = productService.getPictureAndPictureURL(productID)
        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${pictureInfo.second}\"")
            .body(pictureInfo.first)
    }
}
