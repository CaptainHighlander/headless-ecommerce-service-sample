package it.polito.wa2group8.catalog_service.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadCredentialsException(message: String?) : Wa2Exception(message)
