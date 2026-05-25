package com.anmol.web_client_lib.logging

import com.anmol.web_client_lib.logging.context.ReactiveContext.getIdentifiersAsMap
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.SignalType


fun <T: Any> Flux<T>.logOnError(
    errorCode: String? = null,
    errorMessage: String,
    additionalDetails: Map<String, Any> = emptyMap(),
    searchableFields: Map<String, Any> = emptyMap(),
    skipAdditionalDetails: Boolean = false,
    apiCallDetails: ApiCallDetails? = null
): Flux<T> {
    return doOnEach { signal ->
        if (SignalType.ON_ERROR == signal.type) {
            val logger = Logger(this::class.java)
            val throwable = signal.throwable

            val modifiedAdditionalDetails = additionalDetails.toMutableMap()
            if (skipAdditionalDetails) {
                modifiedAdditionalDetails.clear()
            }

            if (throwable is WebClientResponseException) {
                modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = throwable.responseBodyAsString
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
                cause = throwable
            )

            logger.error(details = details, exception = exception)
        }
    }
}

/**
 * Additional details will be encrypted
 */
fun <T: Any> Flux<T>.logOnSuccess(
    message: String,
    additionalDetails: Map<String, Any> = emptyMap(),
    searchableFields: Map<String, Any> = emptyMap(),
    skipAdditionalDetails: Boolean = false,
    skipResponseBody: Boolean = true,
    apiCallDetails: ApiCallDetails? = null
): Flux<T> {
    return doOnEach { signal ->
        if (SignalType.ON_NEXT == signal.type) {

            val modifiedAdditionalDetails = additionalDetails.toMutableMap()
            if (skipAdditionalDetails) {
                modifiedAdditionalDetails.clear()
            }
            if (!skipResponseBody) {
                if (signal.hasValue())
                    modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = signal.get()!! as Any
                else
                    modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = "No response body found"
            }
            val mutableSearchFields = searchableFields.toMutableMap()
            val logger = Logger(this::class.java)
            val logDetails = LogDetails(
                message = createLogMessage(
                    apiCallDetails,
                    message
                ),
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

fun <T : Any> Flux<T>.logOnSuccess(
    messageProvider: (T) -> String,
    additionalDetailsProvider: (T) -> Map<String, Any> = { emptyMap() },
    searchableFieldsProvider: (T) -> Map<String, Any> = { emptyMap() },
    skipAdditionalDetails: Boolean = false,
    skipResponseBody: Boolean = true,
    apiCallDetails: ApiCallDetails? = null
): Flux<T> {
    return doOnEach { signal ->
        if (SignalType.ON_NEXT == signal.type && signal.hasValue()) {
            val value = signal.get()!!
            val modifiedAdditionalDetails = additionalDetailsProvider(value).toMutableMap()

            if (skipAdditionalDetails) {
                modifiedAdditionalDetails.clear()
            }

            if (!skipResponseBody) {
                modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = value
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

fun <T : Any> Flux<T>.logOnError(
    errorCode: String? = null,
    errorMessageProvider: (Throwable?) -> String,
    additionalDetailsProvider: (Throwable?) -> Map<String, Any> = { emptyMap() },
    searchableFieldsProvider: (Throwable?) -> Map<String, Any> = { emptyMap() },
    skipAdditionalDetails: Boolean = false,
    apiCallDetails: ApiCallDetails? = null
): Flux<T> {
    return doOnEach { signal ->
        if (SignalType.ON_ERROR == signal.type) {
            val logger = Logger(this::class.java)
            val throwable = signal.throwable

            val modifiedAdditionalDetails = additionalDetailsProvider(throwable).toMutableMap()
            if (skipAdditionalDetails) {
                modifiedAdditionalDetails.clear()
            }

            if (throwable is WebClientResponseException) {
                modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = throwable.responseBodyAsString
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
