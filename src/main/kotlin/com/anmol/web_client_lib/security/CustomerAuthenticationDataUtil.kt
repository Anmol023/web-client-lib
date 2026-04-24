package com.anmol.web_client_lib.security

import com.anmol.web_client_lib.security.authorization.AuthorizationAspect
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.util.context.ContextView
import java.util.*
import kotlin.collections.get

@Suppress("UNCHECKED_CAST")
@Component
class CustomerAuthenticationDataUtil() {

    companion object {
        private const val TOKEN_DELIMITER = "."
        private const val PAYLOAD_INDEX = 1
        private const val MOBILE_NUMBER = "mobileNumber"
        private const val LOGIN_METADATA = "loginMetadata"
        private const val SOURCE_CHANNEL = "sourceChannel"
        private const val AUTHENTICATION_LEVEL = "authenticationLevel"
    }

    /**
     * Fetches CustomerAuthenticationData from the Reactor context.
     * Throws an error if not present.
     */
    fun fetchCustomerAuthenticationData(): Mono<CustomerAuthenticationData> = Mono.deferContextual { context ->
        context.getOrEmpty<CustomerAuthenticationData>(CustomerAuthenticationData::class.java)
            .map { Mono.just(it) }
            .orElseGet{ Mono.error(IllegalStateException("No authenticated customer data found in context")) }
    }

}
