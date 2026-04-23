package com.example.web_client_lib.logging

import org.springframework.http.HttpHeaders
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.util.context.ContextView


class LoggingContext(
    context: ContextView
) {
    private val referenceId: String?
    private val loginSource: String

    init {
        val headers = httpHeadersFrom(context)
        referenceId = applicationReferenceIdFrom(headers)
        loginSource = loginSourceFrom(headers)
    }

    private fun applicationReferenceIdFrom(headers: HttpHeaders): String? {
        return headers[LogHeaders.APPLICATION_REFERENCE_ID]?.firstOrNull()
    }

    private fun loginSourceFrom(headers: HttpHeaders): String {
        return headers[LogHeaders.LOGIN_SOURCE]?.firstOrNull() ?: "missing-login-source"
    }

    private fun httpHeadersFrom(context: ContextView): HttpHeaders {
        return when {
            context.isEmpty -> HttpHeaders.EMPTY
            context.hasKey(ServerWebExchange::class.java) -> {
                val headersFromRequest = context.get(ServerWebExchange::class.java).request.headers
                if (context.hasKey("headers")) {
                    val headers = HttpHeaders()
                    val headersFromContext = context.get<HttpHeaders>("headers")
                    headersFromContext.forEach { t, u -> headers[t] = u }
                    headersFromRequest.forEach { t, u -> headers[t] = u }
                    headers
                } else headersFromRequest
            }
            context.hasKey("headers") -> context.get("headers")
            else -> HttpHeaders.EMPTY
        }
    }

    fun applicationReferenceIdOrElse(default: String = "missing-reference-id"): String {
        return referenceId ?: default
    }

    companion object {
        fun fromSubscriberContext(): Mono<LoggingContext> = Mono.deferContextual {
            Mono.just(LoggingContext(it))
        }
    }

    object LogHeaders {
        const val APPLICATION_REFERENCE_ID = "reference-id"
        const val LOGIN_SOURCE = "login-source"
    }
}