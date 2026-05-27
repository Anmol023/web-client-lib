package com.anmol.web_client_lib.security

import com.anmol.web_client_lib.expection_handling.UnauthorizedException
import com.anmol.web_client_lib.expection_handling.WebClientError
import com.anmol.web_client_lib.logging.ApiCallDetails
import com.anmol.web_client_lib.logging.logOnError
import com.anmol.web_client_lib.logging.logOnSuccess
import com.anmol.web_client_lib.security.config.TokenServiceProperties
import com.anmol.web_client_lib.web_client.WebClientWrapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI


@Component
@ConditionalOnMissingBean(TokenServiceLocal::class)
class TokenValidationService(
    @Autowired private val webClientWrapper: WebClientWrapper,
    @Autowired private val claimsMapper: ClaimsMapper,
    @Autowired private val properties: TokenServiceProperties
) {
    @Suppress("UNCHECKED_CAST")
    fun validate(token: String, requestUrl: String): Mono<CustomerAuthenticationData> {
        val uri = URI(properties.validationUrl)

        return webClientWrapper.post(
            baseUrl = "${uri.scheme}://${uri.host}",
            path = uri.path,
            body = RequestToken(token, requestUrl),
            returnType = Map::class.java,
            apiCallDetails = ApiCallDetails(
                destinationService = uri.toString(),
                apiName = "token-validate"
            )
        ).map { resp ->
            val valid = resp["valid"] == true
            val claims = resp["claims"] as? Map<String, Any>
                ?: throw UnauthorizedException(
                    WebClientError.WEB1501,
                    cause = IllegalArgumentException("Missing claims in response")
                )
            if (!valid) {
                throw UnauthorizedException(
                    WebClientError.WEB1501,
                    cause = IllegalArgumentException("Invalid token")
                )
            }
            properties.requiredClaims.forEach { claim ->
                if (!claims.containsKey(claim)) {
                    throw UnauthorizedException(
                        WebClientError.WEB1501,
                        cause = IllegalArgumentException("Missing required claim: $claim")
                    )
                }
            }
            claimsMapper.map(claims)
        }.onErrorMap { ex ->
            ex as? UnauthorizedException ?: UnauthorizedException(WebClientError.WEB1501, cause = ex)
        }.logOnSuccess("Successfully validated token for $requestUrl")
            .logOnError(errorMessage = "Failed to validate token for $requestUrl")
    }
}

@Profile(*["local", "test"])
@Component
class TokenServiceLocal : TokenValidationService(WebClientWrapper(WebClient.create()), DefaultClaimsMapper(), TokenServiceProperties()) {
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