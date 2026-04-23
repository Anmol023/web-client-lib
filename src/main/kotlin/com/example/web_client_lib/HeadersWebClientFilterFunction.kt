package com.example.web_client_lib

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.util.context.ContextView

// Used to add headers in request for example (one m-s calls another m-s then the headers present in that will be propagated to second, without explicitly passing)
class HeadersWebClientFilterFunction : ExchangeFilterFunction {
    companion object {
        const val HEADERS = "headers"
    }

    override fun filter(clientRequest: ClientRequest, exchangeFunction: ExchangeFunction): Mono<ClientResponse> {
        return Mono.deferContextual { context ->
            val headers = extractHeaders(context)
            val request = ClientRequest.from(clientRequest)
                .setHeaderFromContextIfNotPresentInRequest(AUTHORIZATION, authToken(headers))
                .appendHeaderIfNonNull("reference-id", referenceId(headers, context))
                .appendHeadersHavingPrefixIfNotEmpty("toggle-for-api", headers)
                .build()

            exchangeFunction.exchange(request)
        }
    }

    private fun extractHeaders(context: ContextView?): HttpHeaders {
        if (context == null || context.isEmpty) {
            return HttpHeaders.EMPTY
        }

        return when {
            context.hasKey(ServerWebExchange::class.java) -> {
                val headers = context.get(ServerWebExchange::class.java).request.headers
                if (headers[AUTHORIZATION].isNullOrEmpty() && context.hasKey(HEADERS)) {
                    setAuthorizationInHeaders(headers, context)
                } else headers
            }
            context.hasKey(HEADERS) -> context.get(HEADERS)
            else -> HttpHeaders.EMPTY
        }
    }

    private fun setAuthorizationInHeaders(headers: HttpHeaders, context: ContextView): HttpHeaders {
        val httpHeaders = HttpHeaders()
        httpHeaders.addAll(headers)
        val contextHeaders = (context.get(HEADERS) as HttpHeaders).getOrEmpty(AUTHORIZATION)
        contextHeaders.firstOrNull()?.let { httpHeaders.add(AUTHORIZATION, it) }
        return httpHeaders
    }

    private fun authToken(headers: HttpHeaders): String? = headers[AUTHORIZATION]?.first()

    private fun referenceId(headers: HttpHeaders, context: ContextView): String? {
        val referenceIdFromHeaderOrContext = headers["reference-id"]?.first() ?: context.getOrDefault("reference-id", null as String?)
        return referenceIdFromHeaderOrContext.let{ if(!it.isNullOrEmpty()) it else null }
    }



    private fun ClientRequest.Builder.appendHeadersHavingPrefixIfNotEmpty(prefix: String, headers: HttpHeaders): ClientRequest.Builder {
        return this.headers { existingHeaders ->
            headers.filter { header ->
                header.key.startsWith(prefix = prefix, ignoreCase = true)
            }.map { toggleHeader -> existingHeaders.add(toggleHeader.key, toggleHeader.value.first()) }
        }
    }

    private fun ClientRequest.Builder.appendHeaderIfNonNull(headerName: String, value: String?): ClientRequest.Builder {
        if (value.isNullOrEmpty())
            return this
        return this.header(headerName, value)
    }

    private fun ClientRequest.Builder.setHeaderFromContextIfNotPresentInRequest(headerName: String, headerValue: String?): ClientRequest.Builder {
        if (headerValue.isNullOrEmpty()) {
            return this
        }
        return this.headers {
            when (it.getFirst(headerName)) {
                null -> it.set(headerName, headerValue)
            }
        }
    }
}



