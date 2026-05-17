package com.anmol.web_client_lib.security

import com.anmol.web_client_lib.expection_handling.UnauthorizedException
import com.anmol.web_client_lib.expection_handling.WebClientError
import com.anmol.web_client_lib.security.config.TokenServiceProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@Component
@ConditionalOnMissingBean(TokenServiceLocal::class)
class TokenValidationService(
    @Autowired private val webClient: WebClient,
    @Autowired private val claimsMapper: ClaimsMapper,
    @Autowired private val properties: TokenServiceProperties
) {
    @Suppress("UNCHECKED_CAST")
    fun validate(token: String, requestUrl: String): Mono<CustomerAuthenticationData> {
        return webClient
            .post()
            .uri(properties.validationUrl)
            .bodyValue(RequestToken(token, requestUrl))
            .retrieve()
            .bodyToMono(Map::class.java)
            .flatMap { resp ->
                val valid = resp["valid"] == true
                val claims = resp["claims"] as? Map<String, Any>
                if (!valid || claims == null) return@flatMap Mono.error(UnauthorizedException(WebClientError.WEB1501, cause = IllegalArgumentException("Invalid token or missing claims in response")))
                // Enforce required claims from properties
                for (claim in properties.requiredClaims) {
                    if (!claims.containsKey(claim)) {
                        return@flatMap Mono.error(UnauthorizedException(WebClientError.WEB1501, cause = IllegalArgumentException("Missing required claim: $claim")))
                    }
                }
                try {
                    Mono.just(claimsMapper.map(claims))
                } catch (ex: Exception) {
                    Mono.error(UnauthorizedException(WebClientError.WEB1501, cause = ex))
                }
            }
    }
}

@Profile(*["local", "test"])
@Component
class TokenServiceLocal : TokenValidationService(WebClient.create(), DefaultClaimsMapper(), TokenServiceProperties()) {
    override fun validate(token: String, requestUrl: String): Mono<CustomerAuthenticationData> {
        return Mono.just(
            CustomerAuthenticationData(
                id = "9999999999",
                loginMetadata = emptyMap(),
                role = Role.CUSTOMER
            )
        )
    }

}