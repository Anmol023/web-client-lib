package com.anmol.web_client_lib.security

import com.anmol.web_client_lib.logging.logOnError
import com.anmol.web_client_lib.logging.logOnSuccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
@ConditionalOnMissingBean(ExternalTokenValidationServiceLocal::class)
class ExternalTokenValidationService(
    @Autowired private val webClient: WebClient,
    @Value("\${axis.token-service.external-token.validation-url:}") val authServiceExternalTokenValidationUrl: String
) {

    fun validate(externalSystemAuthenticationToken: ExternalSystemAuthenticationToken): Mono<Boolean> {
        val validateAuthTokenRequest = ValidateAuthTokenRequest(
            token = externalSystemAuthenticationToken.token,
            externalSystemType = externalSystemAuthenticationToken.externalSystemType
        )
        return  webClient
            .post()
            .uri(authServiceExternalTokenValidationUrl)
            .bodyValue(validateAuthTokenRequest)
            .retrieve()
            .bodyToMono(mutableMapOf<String, Boolean>()::class.java)
            .map { it["valid"] == true }
            .onErrorReturn(false)
            .logOnSuccess(message = "Validation result received for external token")
            .logOnError(errorMessage = "Error in validating external token")
    }
}

data class ValidateAuthTokenRequest(val token: String, val externalSystemType: ExternalSystemType)


@Profile(*["local", "test"])
@Component
class ExternalTokenValidationServiceLocal : ExternalTokenValidationService(WebClient.create(), "") {
    override fun validate(externalSystemAuthenticationToken: ExternalSystemAuthenticationToken): Mono<Boolean> {
        return Mono.just(true)
    }
}