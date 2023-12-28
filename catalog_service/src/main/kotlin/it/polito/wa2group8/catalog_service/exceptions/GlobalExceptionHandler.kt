package it.polito.wa2group8.catalog_service.exceptions

import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import java.util.*


@Component
class CustomAttributes: DefaultErrorAttributes()
{
    override fun getErrorAttributes(request: ServerRequest?, options: ErrorAttributeOptions?): MutableMap<String, Any>
    {
        val errorAttributesMap = super.getErrorAttributes(request, options)
        val throwable = getError(request)
        if (throwable is Wa2Exception)
            errorAttributesMap["message"] = throwable.message
        return errorAttributesMap
    }
}


@Component
@Order(-2)
class GlobalExceptionHandler(val errorAttributes: ErrorAttributes,
                             resourceProperties: WebProperties.Resources,
                             applicationContext: ApplicationContext,
                             codecConfigurer: ServerCodecConfigurer
): AbstractErrorWebExceptionHandler(errorAttributes, resourceProperties, applicationContext)
{
    init
    {
        this.setMessageWriters(codecConfigurer.writers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes?): RouterFunction<ServerResponse>
    {
        return RouterFunctions.route(RequestPredicates.all(), this::formatErrorResponse)
    }

    private fun formatErrorResponse(request: ServerRequest): Mono<ServerResponse>
    {
        val errorAttributesMap: MutableMap<String, Any> = getErrorAttributes(request, ErrorAttributeOptions.defaults())
        val status =  Optional.ofNullable(errorAttributesMap["status"]).orElse(500) as Int
        return ServerResponse
            .status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(errorAttributesMap))
    }
}
