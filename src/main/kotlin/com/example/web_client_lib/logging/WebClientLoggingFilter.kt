package com.example.web_client_lib.logging

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

@Component
open class WebClientLoggingFilter(
    @Value("\${axis.data.log.log-additional-request-response-headers:true}")
    val loggingHeadersEnabled: Boolean = true
) : ExchangeFilterFunction {

    private val logger = Logger(this::class.java)

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val invocationTime = System.currentTimeMillis()
        return LoggingContext.fromSubscriberContext()
            .doOnSuccess {
                if (loggingHeadersEnabled)
                    logger.info(
                        LogDetails(
                            message = "Sending HTTP request",
                            requestMethod = request.method(),
                            requestHeaders = request.headers().filter { header ->
                                val key1 = header.key
                                key1.lowercase() !in listOf("authorization", "x-marketplace-app-secret")
                            },
                            uriWithParams = createUrlWithParamsForLogging(request),
                            referenceId = it.applicationReferenceIdOrElse(),
                            additionalDetails = mapOf("invocationTime" to invocationTime),
                        )
                    )
            }
            .flatMap { context ->
                next.exchange(request)
                    .doOnSuccess { clientResponse ->
                        val statusCode = clientResponse.statusCode()
                        if (statusCode.is2xxSuccessful || statusCode.is3xxRedirection) {
                            if (loggingHeadersEnabled)
                                logger.info(
                                    details = LogDetails(
                                        message = "Received HTTP response",
                                        uriWithParams = createUrlWithParamsForLogging(request),
                                        responseCode = statusCode.value().toString(),
                                        responseHeaders = clientResponse.headers().asHttpHeaders(),
                                        responseStatus = statusCode.toHttpStatus().reasonPhrase,
                                        responseTime = System.currentTimeMillis() - invocationTime,
                                        referenceId = context.applicationReferenceIdOrElse(),
                                    )
                                )
                        } else {
                            logger.error(
                                details = LogDetails(
                                    errorCode = "THAN-3001",
                                    message = "Web request failed",
                                    uriWithParams = createUrlWithParamsForLogging(request),
                                    responseCode = statusCode.value().toString(),
                                    responseHeaders = clientResponse.headers().asHttpHeaders(),
                                    responseTime = System.currentTimeMillis() - invocationTime,
                                    referenceId = context.applicationReferenceIdOrElse(),
                                ), exception = Throwable("Web Client exception")
                            )
                        }
                    }
                    .doOnError {
                        logger.error(
                            details = LogDetails(
                                errorCode = "ERROR-101",
                                message = "Web request failed",
                                referenceId = context.applicationReferenceIdOrElse(),
                                uriWithParams = createUrlWithParamsForLogging(request),
                                responseTime = System.currentTimeMillis() - invocationTime,
                            ),
                            exception = it
                        )
                    }
            }
    }

    open fun createUrlWithParamsForLogging(request: ClientRequest) = request.url().toString()

    fun HttpStatusCode.toHttpStatus() = HttpStatus.resolve(value()) ?: HttpStatus.INTERNAL_SERVER_ERROR
}