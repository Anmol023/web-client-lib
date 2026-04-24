package com.anmol.web_client_lib.web_client

import com.anmol.web_client_lib.logging.LogConstants.IDENTIFIERS_HEADER
import com.anmol.web_client_lib.logging.LogDetails
import com.anmol.web_client_lib.logging.Logger
import org.springframework.boot.actuate.web.exchanges.HttpExchange
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository
import org.springframework.http.HttpMethod

class AccessLoggingTraceRepository : InMemoryHttpExchangeRepository() {
    companion object {
        private val logger = Logger(AccessLoggingTraceRepository::class.java)
    }

    override fun add(trace: HttpExchange) {
        super.add(trace)

        val request = trace.request

        val logDetails = LogDetails(
            message = "Logging request, response headers for: ${request.uri}",
            requestMethod = HttpMethod.valueOf(request.method.uppercase()),
            requestHeaders = request.headers.filter { header ->
                val key1 = header.key
                key1.lowercase() !in listOf("authorization", "x-marketplace-app-secret")
            },
            responseCode = trace.response.status.toString(),
            responseHeaders = trace.response.headers,
            identifiers = request.getIdentifiersOrDefault(),
        )

        logger.info(logDetails)
    }
}

fun HttpExchange.Request.getIdentifiersOrDefault() =
    headers[IDENTIFIERS_HEADER]?.firstOrNull()?.split(";")?.mapNotNull {
        val parts = it.split(":")
        if (parts.size == 2) {
            parts[0].trim() to parts[1].trim()
        } else null
    }?.toMap() ?: emptyMap()
