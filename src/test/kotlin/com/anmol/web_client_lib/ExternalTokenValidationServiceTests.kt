package com.anmol.web_client_lib

import com.anmol.web_client_lib.security.*
import com.anmol.web_client_lib.expection_handling.UnauthorizedException
import com.anmol.web_client_lib.expection_handling.WebClientError
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ExternalTokenValidationServiceTests {
    private val validClaims = mapOf("mobileNumber" to "1234567890", "role" to "customer")
    private val validResponse = mapOf("valid" to true, "claims" to validClaims)
    private val invalidResponse = mapOf("valid" to false)
    private val claimsMapper = DefaultClaimsMapper()
    private val properties = TokenServiceProperties().apply {
        validationUrl = "http://test"
        externalTokenValidationUrl = "http://test-ext"
        requiredClaims = listOf("mobileNumber", "role")
    }

    @Test
    fun `valid external token returns authentication data`() {
        val webClient = Mockito.mock(WebClient::class.java)
        val service = ExternalTokenValidationServiceStub(webClient, claimsMapper, properties, validResponse)
        val token = ExternalSystemAuthenticationToken("token", requestUrl = "url", externalSystemType = ExternalSystemType.external)
        StepVerifier.create(service.validate(token))
            .expectNextMatches { it.id == "1234567890" }
            .verifyComplete()
    }

    @Test
    fun `invalid external token throws UnauthorizedException`() {
        val webClient = Mockito.mock(WebClient::class.java)
        val service = ExternalTokenValidationServiceStub(webClient, claimsMapper, properties, invalidResponse)
        val token = ExternalSystemAuthenticationToken("token", requestUrl = "url", externalSystemType = ExternalSystemType.external)
        StepVerifier.create(service.validate(token))
            .expectError(UnauthorizedException::class.java)
            .verify()
    }

    @Test
    fun `missing required claim throws UnauthorizedException`() {
        val claims = mapOf("mobileNumber" to "1234567890") // missing role
        val response = mapOf("valid" to true, "claims" to claims)
        val webClient = Mockito.mock(WebClient::class.java)
        val service = ExternalTokenValidationServiceStub(webClient, claimsMapper, properties, response)
        val token = ExternalSystemAuthenticationToken("token", requestUrl = "url", externalSystemType = ExternalSystemType.external)
        StepVerifier.create(service.validate(token))
            .expectError(UnauthorizedException::class.java)
            .verify()
    }
}

class ExternalTokenValidationServiceStub(
    webClient: WebClient,
    val claimsMapper: ClaimsMapper, // make claimsMapper a val so it is accessible
    val properties: TokenServiceProperties,
    private val response: Map<String, Any>
) : ExternalTokenValidationService(webClient, claimsMapper, properties) {
    @Suppress("UNCHECKED_CAST")
    override fun validate(externalSystemAuthenticationToken: ExternalSystemAuthenticationToken): Mono<CustomerAuthenticationData> {
        val valid = response["valid"] == true
        val claims = response["claims"] as? Map<String, Any>
        if (!valid || claims == null) return Mono.error(UnauthorizedException(WebClientError.WEB1501))
        for (claim in properties.requiredClaims) {
            if (!claims.containsKey(claim)) {
                return Mono.error(UnauthorizedException(WebClientError.WEB1501))
            }
        }
        // Use the claimsMapper passed to the stub, not the superclass's private property
        return try {
            Mono.just(claimsMapper.map(claims))
        } catch (e: Exception) {
            Mono.error(UnauthorizedException(WebClientError.WEB1501, cause = e))
        }
    }
}
