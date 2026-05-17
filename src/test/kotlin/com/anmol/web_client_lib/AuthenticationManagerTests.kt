package com.anmol.web_client_lib

import com.anmol.web_client_lib.security.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class AuthenticationManagerTests {
    @Test
    fun `authenticate with AuthenticationToken returns authenticated`() {
        val tokenService = Mockito.mock(TokenValidationService::class.java)
        val externalService = Mockito.mock(ExternalTokenValidationService::class.java)
        val manager = AuthenticationManager(tokenService, externalService)
        val auth = AuthenticationToken("token", requestUrl = "url")
        Mockito.`when`(tokenService.validate("token", "url")).thenReturn(Mono.just(CustomerAuthenticationData("id")))
        StepVerifier.create(manager.authenticate(auth))
            .expectNextMatches { it.isAuthenticated }
            .verifyComplete()
    }

    @Test
    fun `authenticate with ExternalSystemAuthenticationToken returns authenticated`() {
        val tokenService = Mockito.mock(TokenValidationService::class.java)
        val externalService = Mockito.mock(ExternalTokenValidationService::class.java)
        val manager = AuthenticationManager(tokenService, externalService)
        val auth = ExternalSystemAuthenticationToken("token", requestUrl = "url", externalSystemType = ExternalSystemType.external)
        Mockito.`when`(externalService.validate(auth)).thenReturn(Mono.just(CustomerAuthenticationData("id")))
        StepVerifier.create(manager.authenticate(auth))
            .expectNextMatches { it.isAuthenticated }
            .verifyComplete()
    }

    @Test
    fun `authenticate with unknown type throws CredentialsExpiredException`() {
        val tokenService = Mockito.mock(TokenValidationService::class.java)
        val externalService = Mockito.mock(ExternalTokenValidationService::class.java)
        val manager = AuthenticationManager(tokenService, externalService)
        val unknownAuth = Mockito.mock(Authentication::class.java)
        StepVerifier.create(manager.authenticate(unknownAuth))
            .expectError(CredentialsExpiredException::class.java)
            .verify()
    }
}

