package com.anmol.web_client_lib.security

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Suppress("UNCHECKED_CAST")
@Component
class CustomerAuthenticationDataUtil {

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
