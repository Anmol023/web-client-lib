package com.anmol.web_client_lib.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthenticationTokenDataResolver(
    @Autowired val customerAuthenticationDataUtil: CustomerAuthenticationDataUtil
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return IAuthenticationData::class.java.isAssignableFrom(parameter.parameterType)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<Any> {
        return Mono.deferContextual { context ->
            context.getOrEmpty<CustomerAuthenticationData>(CustomerAuthenticationData::class.java)
                .map { Mono.just(it) }
                .orElseGet { Mono.error(IllegalStateException("No authenticated customer data found in context")) }
        }
    }

}

