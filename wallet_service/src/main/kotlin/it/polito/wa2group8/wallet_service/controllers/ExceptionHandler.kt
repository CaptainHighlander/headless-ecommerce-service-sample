package it.polito.wa2group8.wallet_service.controllers

import it.polito.wa2group8.wallet_service.exceptions.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandler: ResponseEntityExceptionHandler()
{
    @ExceptionHandler(value=[BadRequestException::class])
    fun handleBadRequest(ex: BadRequestException, request: WebRequest): ResponseEntity<Any>
    {
        return ResponseEntity.badRequest().body(ex.message)
    }

    @ExceptionHandler(value=[ForbiddenException::class])
    fun handleForbidden(ex: ForbiddenException, request: WebRequest): ResponseEntity<Any>
    {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.message)
    }

    @ExceptionHandler(value=[NotFoundException::class])
    fun handleNotFound(ex: NotFoundException, request: WebRequest): ResponseEntity<Any>
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)
    }
}
