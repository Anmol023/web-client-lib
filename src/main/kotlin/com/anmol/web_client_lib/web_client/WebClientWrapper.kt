package com.anmol.web_client_lib.web_client

import com.anmol.web_client_lib.expection_handling.BadDataException
import com.anmol.web_client_lib.expection_handling.BaseException
import com.anmol.web_client_lib.expection_handling.DataNotFound
import com.anmol.web_client_lib.expection_handling.ErrorResponse
import com.anmol.web_client_lib.expection_handling.UnauthorizedException
import com.anmol.web_client_lib.expection_handling.UnprocessableEntityException
import com.anmol.web_client_lib.expection_handling.ValidationErrorDetails
import com.anmol.web_client_lib.expection_handling.ValidationException
import com.anmol.web_client_lib.logging.ApiCallDetails
import com.anmol.web_client_lib.logging.logOnError
import com.anmol.web_client_lib.logging.logOnSuccess
import com.anmol.web_client_lib.serializer.ObjectMapperCache.objectMapperCache
import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime

/*
    This is the class should be used for any internal service call.
    The client takes care of logging, exception handling, common headers, timeout.
 */
@Suppress("UNCHECKED_CAST")
class WebClientWrapper(private val webClient: WebClient) {
    private val defaultRequestTimeout = Duration.ofMillis(300000)

    fun <T: Any> get(
        baseUrl: String,
        path: String,
        returnType: Class<T>,
        queryParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        uriVariables: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        requestTimeout: Duration? = null,
        skipLoggingResponseBody: Boolean = false,
        apiCallDetails: ApiCallDetails
    ): Mono<T> {
        val url =
            baseUrl + UriComponentsBuilder.fromPath(path).uriVariables(uriVariables).queryParams(queryParams).build()
                .toUriString()

        return webClient.get()
            .uri(url)
            .headers { h ->  headers.map {  h.set(it.key, it.value) } }
            .retrieve()
            .bodyToMono(returnType)
            .timeout(requestTimeout ?: defaultRequestTimeout)
            .logOnSuccess(
                message = "GET request to Service successful",
                skipResponseBody = skipLoggingResponseBody,
                searchableFields = mapOf(
                    "uriWithParams" to url
                ),
                apiCallDetails = apiCallDetails
            ).logOnError(
                errorCode = "",
                errorMessage = "GET request to Service failed",
                searchableFields = mapOf(
                    "uriWithParams" to url
                ),
                apiCallDetails = apiCallDetails
            )
            .contextWrite { it.put("startTime", LocalDateTime.now()) }
            .onErrorMap(this::handleError)
    }

    fun <T: Any> post(
        baseUrl: String,
        path: String,
        body: Any,
        returnType: Class<T>,
        queryParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        uriVariables: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        requestTimeout: Duration? = null,
        skipLoggingRequestBody: Boolean = false,
        skipLoggingResponseBody: Boolean = false,
        apiCallDetails: ApiCallDetails
    ): Mono<T> {
        val url =
            baseUrl + UriComponentsBuilder.fromPath(path).uriVariables(uriVariables).queryParams(queryParams).build()
                .toUriString()

        return webClient.post()
            .uri(url)
            .headers { h ->  headers.map {  h.set(it.key, it.value) } }
            .bodyValue(body)
            .retrieve()
            .bodyToMono(returnType)
            .timeout(requestTimeout ?: defaultRequestTimeout)
            .logOnSuccess(
                "POST request to Service successful",
                additionalDetails = mapOf("requestBody" to body),
                skipAdditionalDetails = skipLoggingRequestBody,
                skipResponseBody = skipLoggingResponseBody,
                searchableFields = mapOf(
                    "uriWithParams" to url
                ),
                apiCallDetails = apiCallDetails
            )
            .logOnError(
                errorCode = "",
                errorMessage = "POST request to Service failed",
                additionalDetails = mapOf("requestBody" to body),
                skipAdditionalDetails = skipLoggingRequestBody,
                searchableFields = mapOf(
                    "uriWithParams" to url
                ),
                apiCallDetails = apiCallDetails
            )
            .contextWrite { it.put("startTime", LocalDateTime.now()) }
            .onErrorMap(this::handleError)
    }

    fun <T: Any> patch(
        baseUrl: String,
        path: String, body: Any,
        returnType: Class<T>,
        queryParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        uriVariables: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        requestTimeout: Duration? = null,
        skipLoggingRequestBody: Boolean = false,
        skipLoggingResponseBody: Boolean = false,
        apiCallDetails: ApiCallDetails
    ): Mono<T> {
        val url =
            baseUrl + UriComponentsBuilder.fromPath(path).uriVariables(uriVariables).queryParams(queryParams).build()
                .toUriString()

        return webClient.patch()
            .uri(url)
            .headers { h ->  headers.map {  h.set(it.key, it.value) } }
            .bodyValue(body)
            .retrieve()
            .bodyToMono(returnType)
            .timeout(requestTimeout ?: defaultRequestTimeout)
            .logOnSuccess(
                "PATCH request to Service successful",
                additionalDetails = mapOf("requestBody" to body),
                skipAdditionalDetails = skipLoggingRequestBody,
                skipResponseBody = skipLoggingResponseBody,
                searchableFields = mapOf(
                    "uriWithParams" to url
                ),
                apiCallDetails = apiCallDetails
            )
            .logOnError(
                errorCode = "",
                errorMessage = "PATCH request to Service failed",
                additionalDetails = mapOf("requestBody" to body),
                skipAdditionalDetails = skipLoggingRequestBody,
                searchableFields = mapOf(
                    "uriWithParams" to url
                ),
                apiCallDetails = apiCallDetails
            )
            .contextWrite {it.put("startTime", LocalDateTime.now()) }
            .onErrorMap(this::handleError)
    }

    fun <T: Any> put(
        baseUrl: String,
        path: String, body: Any,
        returnType: Class<T>,
        queryParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        uriVariables: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        requestTimeout: Duration? = null,
        skipLoggingRequestBody: Boolean = false,
        skipLoggingResponseBody: Boolean = false,
        apiCallDetails: ApiCallDetails
    ): Mono<T> {
        val url =
            baseUrl + UriComponentsBuilder.fromPath(path).uriVariables(uriVariables).queryParams(queryParams).build()
                .toUriString()

        return webClient.put()
            .uri(url)
            .headers { h ->  headers.map {  h.set(it.key, it.value) } }
            .bodyValue(body)
            .retrieve()
            .bodyToMono(returnType)
            .timeout(requestTimeout ?: defaultRequestTimeout)
            .logOnSuccess(
                "PUT request to Service successful",
                additionalDetails = mapOf("requestBody" to body),
                skipAdditionalDetails = skipLoggingRequestBody,
                skipResponseBody = skipLoggingResponseBody,
                searchableFields = mapOf(
                    "uriWithParams" to url
                ),
                apiCallDetails = apiCallDetails
            )
            .logOnError(
                errorCode = "",
                errorMessage = "PUT request to Service failed",
                additionalDetails = mapOf("requestBody" to body),
                skipAdditionalDetails = skipLoggingRequestBody,
                searchableFields = mapOf(
                    "uriWithParams" to url
                ),
                apiCallDetails = apiCallDetails
            )
            .contextWrite {it.put("startTime", LocalDateTime.now()) }
            .onErrorMap(this::handleError)
    }

    fun <T: Any> delete(
        baseUrl: String,
        path: String,
        returnType: Class<T>,
        queryParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        uriVariables: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        requestTimeout: Duration? = null,
        skipLoggingResponseBody: Boolean = false,
        apiCallDetails: ApiCallDetails
    ): Mono<T> {
        val url =
            baseUrl + UriComponentsBuilder.fromPath(path).uriVariables(uriVariables).queryParams(queryParams).build()
                .toUriString()

        return webClient.delete()
            .uri(url)
            .headers { h ->  headers.map {  h.set(it.key, it.value) } }
            .retrieve()
            .bodyToMono(returnType)
            .timeout(requestTimeout ?: defaultRequestTimeout)
            .logOnSuccess(
                message = "DELETE request to Service successful",
                skipResponseBody = skipLoggingResponseBody,
                searchableFields = mapOf(
                    "uriWithParams" to url
                ),
                apiCallDetails = apiCallDetails
            ).logOnError(
                errorCode = "",
                errorMessage = "DELETE request to Service failed",
                searchableFields = mapOf(
                    "uriWithParams" to url
                ),
                apiCallDetails = apiCallDetails
            )
            .contextWrite { it.put("startTime", LocalDateTime.now()) }
            .onErrorMap(this::handleError)
    }

    private fun handleError(error: Throwable): Throwable {
        if (error !is WebClientResponseException) return error

        return when {
            (error.statusCode == HttpStatus.EXPECTATION_FAILED) -> validationException(error)
            (error.statusCode == HttpStatus.NOT_FOUND) -> dataNotFound(error)
            (error.statusCode == HttpStatus.UNPROCESSABLE_ENTITY) -> unprocessableEntityException(error)
            (error.statusCode == HttpStatus.BAD_REQUEST) -> badDataException(error)
            (error.statusCode == HttpStatus.UNAUTHORIZED) -> unauthorized(error)
            else -> baseException(error)
        }
    }

    private fun validationException(error: WebClientResponseException): ValidationException {
        val errorResponse = objectMapperCache.readValue(error.responseBodyAsString, ValidationErrorDetails::class.java)
        return ValidationException(errorResponse, error.localizedMessage)
    }

    private fun dataNotFound(error: WebClientResponseException): DataNotFound {
        return DataNotFound(
            serviceError = deserializeToErrorResponse(error).toServiceError(),
            details = deserializeToMap(error)
        )
    }

    private fun unprocessableEntityException(error: WebClientResponseException): UnprocessableEntityException {
        return UnprocessableEntityException(
            serviceError = deserializeToErrorResponse(error).toServiceError(),
            details = deserializeToMap(error)
        )
    }

    private fun badDataException(error: WebClientResponseException): BadDataException {
        return BadDataException(
            serviceError = deserializeToErrorResponse(error).toServiceError(),
            details = deserializeToMap(error)
        )
    }

    private fun baseException(error: WebClientResponseException): BaseException {
        return BaseException(
            serviceError = deserializeToErrorResponse(error).toServiceError(),
            details = deserializeToMap(error)
        )
    }

    private fun unauthorized(error: WebClientResponseException): UnauthorizedException {
        return UnauthorizedException(
            serviceError = deserializeToErrorResponse(error).toServiceError(),
            details = deserializeToMap(error)
        )
    }

    private fun deserializeToMap(error: WebClientResponseException) =
        objectMapperCache.readValue(error.responseBodyAsString, Map::class.java) as Map<String, Any>

    private fun deserializeToErrorResponse(error: WebClientResponseException) =
        objectMapperCache.readValue(error.responseBodyAsString, ErrorResponse::class.java)

    fun registerFilter(exchangeFilterFunction: ExchangeFilterFunction): WebClientWrapper {
        val webClientWithFilter = webClient.mutate().filter(exchangeFilterFunction).build()
        return WebClientWrapper(webClientWithFilter)
    }
}