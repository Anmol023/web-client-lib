package com.example.web_client_lib.logging

import com.example.web_client_lib.serializer.DefaultSerializer
import com.fasterxml.jackson.core.JsonParseException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.*
import reactor.util.context.ContextView
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Additional details will be encrypted
 */
fun <T> Mono<T>.logOnError(
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
            val traceId = getTraceId(signal.contextView)
            val referenceId = getReferenceId(signal.contextView)
            val loginSource = getChannel(signal.contextView)
            val throwable = signal.throwable

            val modifiedAdditionalDetails = additionalDetails.toMutableMap()
            if (skipAdditionalDetails) {
                modifiedAdditionalDetails.clear()
            }

            if (throwable is WebClientResponseException) {
                modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = errorResponseBodyFrom(throwable)
            }
            val mutableSearchFields=searchableFields.toMutableMap()
            mutableSearchFields["channelId"] = loginSource
            val details = LogDetails(
                errorCode = errorCode,
                message = createLogMessage(
                    apiCallDetails,
                    errorMessage
                ),
                referenceId = referenceId,
                additionalDetails = modifiedAdditionalDetails.toMap(),
                searchableFields = mutableSearchFields,
                responseTime = getResponseTime(signal.contextView),
                apiCallDetails = apiCallDetails?.getDetailsToLog()
            )

            val exception = Throwable(
                message = "",
                cause = throwable,
            )

            logger.error(details = details, exception = exception)
        }
    }
}

/**
 * Additional details will be encrypted
 */
fun <T> Mono<T>.logOnSuccess(
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
                    modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = getDeserializedResponseBody<T>(signal)
                else
                    modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = "No response body found"
            }
            val mutableSearchFields = searchableFields.toMutableMap()
            mutableSearchFields["channelId"] = getChannel(signal.contextView)
            val logger = Logger(this::class.java)
            val logDetails = LogDetails(
                message = createLogMessage(apiCallDetails, message),
                traceId = getTraceId(signal.contextView),
                referenceId = getReferenceId(signal.contextView),
                additionalDetails = modifiedAdditionalDetails.toMap(),
                searchableFields = mutableSearchFields,
                logType = getLogType(signal.contextView),
                journeyIds = getJourneyIds(signal.contextView),
                responseTime = getResponseTime(signal.contextView),
                apiCallDetails = apiCallDetails?.getDetailsToLog()
            )
            logger.info(details = logDetails)
        }
    }
}

fun createLogMessage(
    apiCallDetails: ApiCallDetails?,
    message: String
): String {
    return apiCallDetails?.let { "[${apiCallDetails.sourceService}] [${it.destinationService.value}] [${it.apiName.value}] $message" }
        ?: message
}

fun getResponseTime(context: ContextView): Long {
    return context.getOrEmpty<LocalDateTime>(LogConstants.API_CALL_START_TIME)
        .map { (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - it.toEpochSecond(ZoneOffset.UTC)) * 1000 }
        .orElseGet { -1 }
}

private fun <T> getDeserializedResponseBody(signal: Signal<T>): Any {
    val data = signal.get()!!
    return if (data is String) {
        try {
            DefaultSerializer.deserialize(data, Map::class.java)
        } catch (e: JsonParseException) {
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
    } catch (e: Throwable) {
        response
    }
}
