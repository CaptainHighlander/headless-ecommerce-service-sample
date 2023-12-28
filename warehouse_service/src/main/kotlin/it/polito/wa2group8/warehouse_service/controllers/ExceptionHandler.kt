package it.polito.wa2group8.warehouse_service.controllers

import it.polito.wa2group8.warehouse_service.exceptions.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandler: ResponseEntityExceptionHandler()
{
    @ExceptionHandler(value=[BadRequestException::class])
    fun handleBadRequest(ex: BadRequestException, request: WebRequest) = ResponseEntity.badRequest().body(ex.message)

    @ExceptionHandler(value=[NotFoundException::class])
    fun handleNotFound(ex: NotFoundException, request: WebRequest) = ResponseEntity.status(404).body(ex.message)
}
