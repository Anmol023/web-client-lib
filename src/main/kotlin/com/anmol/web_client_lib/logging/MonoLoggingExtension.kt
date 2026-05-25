package com.anmol.web_client_lib.logging

import com.anmol.web_client_lib.logging.context.ReactiveContext.getIdentifiersAsMap
import com.anmol.web_client_lib.serializer.DefaultSerializer
import org.springframework.boot.json.JsonParseException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.*
import reactor.util.context.ContextView
import java.time.LocalDateTime
import java.time.ZoneOffset

fun <T: Any> Mono<T>.logOnError(
    errorCode: String? = null,
    errorMessage: String,
    additionalDetails: Map<String, Any> = emptyMap(),
    searchableFields: Map<String, Any> = emptyMap(),
    skipAdditionalDetails: Boolean = false,
    apiCallDetails: ApiCallDetails? = null
): Mono<T> {
    return doOnEach { signal ->
        if (SignalType.ON_ERROR == signal.type) {
            val logger = Logger(this::class.java)
            val throwable = signal.throwable

            val modifiedAdditionalDetails = additionalDetails.toMutableMap()
            if (skipAdditionalDetails) {
                modifiedAdditionalDetails.clear()
            }

            if (throwable is WebClientResponseException) {
                modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = errorResponseBodyFrom(throwable)
            }
            val mutableSearchFields=searchableFields.toMutableMap()
            val details = LogDetails(
                errorCode = errorCode,
                message = createLogMessage(
                    apiCallDetails,
                    errorMessage
                ),
                additionalDetails = modifiedAdditionalDetails.toMap(),
                searchableFields = mutableSearchFields,
                responseTime = getResponseTime(signal.contextView),
                apiCallDetails = apiCallDetails?.getDetailsToLog(),
                identifiers = getIdentifiersAsMap(signal.contextView)
            )

            val exception = Throwable(
                message = throwable?.message ?: "error occurred without exception message",
                cause = throwable,
            )

            logger.error(details = details, exception = exception)
        }
    }
}

fun <T : Any> Mono<T>.logOnError(
    errorCode: String? = null,
    errorMessageProvider: (Throwable?) -> String,
    additionalDetailsProvider: (Throwable?) -> Map<String, Any> = { emptyMap() },
    searchableFieldsProvider: (Throwable?) -> Map<String, Any> = { emptyMap() },
    skipAdditionalDetails: Boolean = false,
    apiCallDetails: ApiCallDetails? = null
): Mono<T> {
    return doOnEach { signal ->
        if (SignalType.ON_ERROR == signal.type) {
            val logger = Logger(this::class.java)
            val throwable = signal.throwable

            val modifiedAdditionalDetails = additionalDetailsProvider(throwable).toMutableMap()
            if (skipAdditionalDetails) {
                modifiedAdditionalDetails.clear()
            }

            if (throwable is WebClientResponseException) {
                modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = errorResponseBodyFrom(throwable)
            }

            val details = LogDetails(
                errorCode = errorCode,
                message = createLogMessage(apiCallDetails, errorMessageProvider(throwable)),
                additionalDetails = modifiedAdditionalDetails.toMap(),
                searchableFields = searchableFieldsProvider(throwable).toMutableMap(),
                responseTime = getResponseTime(signal.contextView),
                apiCallDetails = apiCallDetails?.getDetailsToLog(),
                identifiers = getIdentifiersAsMap(signal.contextView)
            )

            val exception = Throwable(
                message = throwable?.message ?: "error occurred without exception message",
                cause = throwable
            )

            logger.error(details = details, exception = exception)
        }
    }
}
/**
 * Additional details will be encrypted
 */
fun <T: Any> Mono<T>.logOnSuccess(
    message: String,
    additionalDetails: Map<String, Any> = emptyMap(),
    searchableFields: Map<String, Any> = emptyMap(),
    skipAdditionalDetails: Boolean = false,
    skipResponseBody: Boolean = true,
    apiCallDetails: ApiCallDetails? = null
): Mono<T> {
    return doOnEach { signal ->
        if (SignalType.ON_NEXT == signal.type) {
            val modifiedAdditionalDetails = additionalDetails.toMutableMap()

            if (skipAdditionalDetails) {
                modifiedAdditionalDetails.clear()
            }

            if (!skipResponseBody) {
                if (signal.hasValue())
                    modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = getDeserializedResponseBody(signal)
                else
                    modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = "No response body found"
            }
            val mutableSearchFields = searchableFields.toMutableMap()
            val logger = Logger(this::class.java)
            val logDetails = LogDetails(
                message = createLogMessage(apiCallDetails, message),
                additionalDetails = modifiedAdditionalDetails.toMap(),
                searchableFields = mutableSearchFields,
                responseTime = getResponseTime(signal.contextView),
                apiCallDetails = apiCallDetails?.getDetailsToLog(),
                identifiers = getIdentifiersAsMap(signal.contextView)
            )
            logger.info(details = logDetails)
        }
    }
}

fun <T: Any> Mono<T>.logOnSuccess(
    messageProvider: (T) -> String,
    additionalDetailsProvider: (T) -> Map<String, Any> = { emptyMap() },
    searchableFieldsProvider: (T) -> Map<String, Any> = { emptyMap() },
    skipAdditionalDetails: Boolean = false,
    skipResponseBody: Boolean = true,
    apiCallDetails: ApiCallDetails? = null
): Mono<T> {
    return doOnEach { signal ->
        if (SignalType.ON_NEXT == signal.type && signal.hasValue()) {
            val value = signal.get()!!
            val modifiedAdditionalDetails = additionalDetailsProvider(value).toMutableMap()

            if (skipAdditionalDetails) {
                modifiedAdditionalDetails.clear()
            }

            if (!skipResponseBody) {
                modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = getDeserializedResponseBody(signal)
            }

            val logger = Logger(this::class.java)
            val logDetails = LogDetails(
                message = createLogMessage(apiCallDetails, messageProvider(value)),
                additionalDetails = modifiedAdditionalDetails.toMap(),
                searchableFields = searchableFieldsProvider(value).toMutableMap(),
                responseTime = getResponseTime(signal.contextView),
                apiCallDetails = apiCallDetails?.getDetailsToLog(),
                identifiers = getIdentifiersAsMap(signal.contextView)
            )
            logger.info(details = logDetails)
        }
    }
}

fun createLogMessage(
    apiCallDetails: ApiCallDetails?,
    message: String
): String {
    return apiCallDetails?.let { "[${apiCallDetails.sourceService}] [${it.destinationService}] [${it.apiName}] $message" }
        ?: message
}

fun getResponseTime(context: ContextView): Long {
    return context.getOrEmpty<LocalDateTime>(LogConstants.API_CALL_START_TIME)
        .map { (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - it.toEpochSecond(ZoneOffset.UTC)) * 1000 }
        .orElseGet { -1 }
}

private fun <T: Any> getDeserializedResponseBody(signal: Signal<T>): Any {
    val data = signal.get()!!
    return if (data is String) {
        try {
            DefaultSerializer.deserialize(data, Map::class.java)
        } catch (_: JsonParseException) {
            data
        }
    } else {
        data
    }
}

private fun errorResponseBodyFrom(exception: WebClientResponseException): Any {
    val response = exception.responseBodyAsString
    return try {
        DefaultSerializer.deserialize(response, Map::class.java)
    } catch (_: Throwable) {
        response
    }
}
