package com.anmol.web_client_lib.security

import com.anmol.web_client_lib.expection_handling.ErrorResponse
import com.anmol.web_client_lib.expection_handling.UnauthorizedException
import com.anmol.web_client_lib.expection_handling.WebClientError
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
    @Autowired private val claimsMapper: ClaimsMapper,
    @Autowired private val properties: TokenServiceProperties
) {
    @Suppress("UNCHECKED_CAST")
    fun validate(externalSystemAuthenticationToken: ExternalSystemAuthenticationToken): Mono<CustomerAuthenticationData> {
        val validateAuthTokenRequest = ValidateAuthTokenRequest(
            token = externalSystemAuthenticationToken.token,
            externalSystemType = externalSystemAuthenticationToken.externalSystemType
        )
        return webClient
            .post()
            .uri(properties.externalTokenValidationUrl)
            .bodyValue(validateAuthTokenRequest)
            .retrieve()
            .bodyToMono(Map::class.java)
            .flatMap { resp ->
                val valid = resp["valid"] == true
                val claims = resp["claims"] as? Map<String, Any>
                if (!valid || claims == null) return@flatMap Mono.error(UnauthorizedException(WebClientError.WEB1501, cause = IllegalArgumentException("Invalid token or missing claims")))
                // Enforce required claims from properties
                for (claim in properties.requiredClaims) {
                    if (!claims.containsKey(claim)) {
                        return@flatMap Mono.error(UnauthorizedException(WebClientError.WEB1501, cause = IllegalArgumentException("Missing required claim: $claim")))
                    }
                }
                try {
                    Mono.just(claimsMapper.map(claims))
                } catch (e: Exception) {
                    Mono.error(UnauthorizedException(WebClientError.WEB1501, cause = e))
                }
            }
            .logOnSuccess(message = "Validation result received for external token")
            .logOnError(errorMessage = "Error in validating external token")
    }
}

data class ValidateAuthTokenRequest(val token: String, val externalSystemType: ExternalSystemType)


@Profile(*["local", "test"])
@Component
class ExternalTokenValidationServiceLocal : ExternalTokenValidationService(WebClient.create(), DefaultClaimsMapper(), TokenServiceProperties()) {
    override fun validate(externalSystemAuthenticationToken: ExternalSystemAuthenticationToken): Mono<CustomerAuthenticationData> {
        // Return a dummy CustomerAuthenticationData for local/test
        return Mono.just(
            CustomerAuthenticationData(
                id = "9999999999",
                loginMetadata = emptyMap(),
                role = Role.CUSTOMER
            )
        )
    }
}