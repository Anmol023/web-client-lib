package com.anmol.web_client_lib.logging.context

import com.anmol.web_client_lib.logging.LogConstants.IDENTIFIERS_HEADER
import org.springframework.http.HttpHeaders
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.util.context.ContextView

class LoggingContext(
    context: ContextView
) {
    private val identifiers: Map<String, String>?

    init {
        val headers = httpHeadersFrom(context)
        identifiers = getIdentifiersFrom(headers)
    }

    private fun httpHeadersFrom(context: ContextView): HttpHeaders {
        return when {
            context.isEmpty -> HttpHeaders.EMPTY
            context.hasKey(ServerWebExchange::class.java) -> {
                val headersFromRequest = context.get(ServerWebExchange::class.java).request.headers
                if (context.hasKey("headers")) {
                    val headers = HttpHeaders()
                    val headersFromContext = context.get<HttpHeaders>("headers")
                    headers.addAll(headersFromContext)
                    headers.addAll(headersFromRequest)
                    headers
                } else headersFromRequest
            }
            context.hasKey("headers") -> context.get("headers")
            else -> HttpHeaders.EMPTY
        }
    }

    fun getIdentifiersFrom(headers: HttpHeaders): Map<String, String> {
        val value = headers[IDENTIFIERS_HEADER]?.firstOrNull()

        return value?.split(";")?.mapNotNull {
            val parts = it.split(":")
            if(parts.size == 2) {
                parts[0].trim() to parts[1].trim()
            } else null
        }?.toMap() ?: emptyMap()
    }

    companion object {
        fun fromSubscriberContext(): Mono<LoggingContext> = Mono.deferContextual {
            Mono.just(LoggingContext(it))
        }
    }
}