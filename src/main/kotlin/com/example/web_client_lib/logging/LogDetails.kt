package com.example.web_client_lib.logging

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

data class LogDetails(
    val errorCode: String? = null,
    val message: String,
    @field: JsonSerialize(using = HttpMethodSerializer::class)
    val requestMethod: HttpMethod? = null,
    val requestHeaders: Map<String, Any>? = null,
    val requestBody: String? = null,
    val uriWithParams: String? = null,
    val responseCode: String? = null,
    val responseHeaders: Map<String, Any>? = null,
    val responseStatus: String? = null,
    val responseTime: Long = -1,
    val responseBody: String? = null,
    val referenceId: String? = null,
    val additionalDetails: Map<String, Any> = emptyMap(),
    val searchableFields: Map<String, Any> = emptyMap(),
    val logType: String = "LOGS",
    val apiCallDetails: Map<String, Any>? = null
) {
    companion object {
        fun create(
            message: String,
            errorCode: String? = null,
            requestDetails: RequestDetails? = null,
            responseDetails: ResponseDetails? = null,
            referenceId: String? = null,
            searchableFields: Map<String, Any> = emptyMap(),
            apiCallDetails: ApiCallDetails? = null
        ): LogDetails {
            return LogDetails(
                message = message,
                errorCode = errorCode,
                requestMethod = requestDetails?.requestMethod,
                requestHeaders = requestDetails?.requestHeaders,
                uriWithParams = requestDetails?.uriWithParams,
                requestBody = requestDetails?.requestBody,
                responseCode = responseDetails?.responseCode,
                responseStatus = responseDetails?.responseStatus,
                responseTime = responseDetails?.responseTime ?: -1,
                responseHeaders = responseDetails?.responseHeaders,
                responseBody = responseDetails?.responseBody,
                referenceId = referenceId,
                searchableFields = searchableFields,
                apiCallDetails = apiCallDetails?.getDetailsToLog()
            )
        }
    }
}

data class ApiCallDetails(
    val destinationService: String,
    val apiName: String,
) {
    val sourceService: String = ServiceDetails.getServiceName()

    fun getDetailsToLog(): Map<String, String> {
        return mapOf(
            "sourceService" to sourceService,
            "destinationService" to destinationService,
            "apiName" to apiName
        )
    }
}

@Component
class HttpMethodSerializer : StdSerializer<HttpMethod>(HttpMethod::class.java) {
    private fun readResolve(): Any = HttpMethodSerializer()
    override fun serialize(value: HttpMethod?, gen: JsonGenerator?, p2: SerializerProvider?) {
        if (gen == null) return
        return gen.writeString(value?.name() ?: "")
    }
}

