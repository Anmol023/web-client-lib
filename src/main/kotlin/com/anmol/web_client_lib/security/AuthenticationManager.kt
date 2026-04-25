package com.anmol.web_client_lib.security

import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

class AuthenticationManager(
    private val tokenValidationService: TokenService,
    private val externalTokenValidationService: ExternalTokenValidationService
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return when (authentication) {
            is AuthenticationToken -> {
                tokenValidationService
                    .validate(authentication.token, authentication.requestUrl)
                    .map { authentication.authenticated() }
            }
            is ExternalSystemAuthenticationToken -> {
                externalTokenValidationService.validate(authentication)
                    .map { authentication.authenticated() }
            }
            else -> Mono.error(CredentialsExpiredException("Token expired"))
        }
    }
}