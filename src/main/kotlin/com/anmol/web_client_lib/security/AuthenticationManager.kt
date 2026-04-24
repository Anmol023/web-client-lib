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
                    .flatMap(mapToAuthenticationTokenOrError(authentication))
            }
            is ExternalSystemAuthenticationToken -> {
                externalTokenValidationService.validate(authentication)
                    .flatMap(mapToAuthenticationTokenOrError(authentication))
            }
            else -> Mono.error(CredentialsExpiredException("Token expired"))
        }
    }

    private fun mapToAuthenticationTokenOrError(authentication: AuthenticationToken): (Boolean) -> Mono<UsernamePasswordAuthenticationToken> {
        return {
            if (it) Mono.just(authentication.authenticated())
            else Mono.error(
                CredentialsExpiredException("Token expired")
            )
        }
    }

    private fun mapToAuthenticationTokenOrError(authentication: ExternalSystemAuthenticationToken): (Boolean) -> Mono<UsernamePasswordAuthenticationToken> {
        return {
            if (it) Mono.just(authentication.authenticated())
            else Mono.error(
                CredentialsExpiredException("Token expired")
            )
        }
    }
}