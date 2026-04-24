package com.anmol.web_client_lib.logging.context

import com.anmol.web_client_lib.logging.LogConstants.IDENTIFIERS_HEADER
import org.springframework.web.server.ServerWebExchange
import reactor.util.context.ContextView

internal object ReactiveContext {

    fun getIdentifiersAsMap(context: ContextView): Map<String, String> =
        getRequestHeadersAsMap(context, IDENTIFIERS_HEADER)?: getValueFromSubscriberContextAsMap(context, IDENTIFIERS_HEADER) ?: emptyMap()


    fun getRequestHeadersAsMap(
        context: ContextView,
        key: String
    ): Map<String, String>?{
        val value = if (context.hasKey(ServerWebExchange::class))
            context.get<ServerWebExchange>(ServerWebExchange::class).request.headers.getFirst(key)
        else null

        return value?.split(";")?.associate {
            val (k, v) = it.split(":")
            k to v
        }
    }

    fun getValueFromSubscriberContextAsMap(context: ContextView, key: String): Map<String, String>? {
        return if (context.hasKey(key))
            context.get<String>(key).split(";").associate {
                val (k, v) = it.split(":")
                k to v
            }
        else null
    }
    fun getValueFromSubscriberContext(context: ContextView, key: String): String? {
        return if (context.hasKey(key)) {
            context.get<String>(key)
        } else {
            null
        }
    }

    fun getValueFromHeaders(context: ContextView, key: String): String? {
        return if (context.hasKey(ServerWebExchange::class)) {
            context.get<ServerWebExchange>(ServerWebExchange::class).request.headers.getFirst(key)
        } else {
            null
        }
    }
}