package com.anmol.web_client_lib.security

import com.anmol.web_client_lib.logging.logOnSuccess
import com.anmol.web_client_lib.security.config.SecurityProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.WWW_AUTHENTICATE
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


@Suppress("UNCHECKED_CAST")
@Configuration
@ComponentScan("com.anmol.web_client_lib.security")
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityProperties::class)
class SecurityConfiguration(val securityProperties: SecurityProperties) {

    private val _tokenHeaderName = "x-session-id"

    @Bean
    fun authenticationManager(
        tokenValidationService: TokenValidationService,
        externalTokenValidationService: ExternalTokenValidationService
    ): AuthenticationManager {
        return AuthenticationManager(tokenValidationService, externalTokenValidationService)
    }

    @Bean
    fun tokenAuthenticationFilter(authenticationManager: AuthenticationManager): AuthenticationWebFilter {
        val tokenAuthenticationFilter = AuthenticationWebFilter(authenticationManager)
        tokenAuthenticationFilter.setServerAuthenticationConverter(this::extractToken)
        tokenAuthenticationFilter.setRequiresAuthenticationMatcher(
            NegatedServerWebExchangeMatcher(
                ServerWebExchangeMatchers.matchers(
                    ServerWebExchangeMatchers.pathMatchers(HttpMethod.OPTIONS, "/**"),
                    ServerWebExchangeMatchers.pathMatchers(*securityProperties.unauthenticatedEndpoints)
                )
            )
        )
        tokenAuthenticationFilter.setAuthenticationFailureHandler { webExchange, _ ->
            Mono.fromRunnable {
                mutateExchangeAsUnAuthorized(webExchange)
            }
        }
        return tokenAuthenticationFilter
    }

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        tokenAuthenticationFilter: AuthenticationWebFilter
    ): SecurityWebFilterChain {
        return http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .build()
    }

    private fun mutateExchangeAsUnAuthorized(webFilterExchange: WebFilterExchange) {
        val response = webFilterExchange.exchange.response
        webFilterExchange.exchange.request
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers[WWW_AUTHENTICATE] = "Bearer"
    }

    private fun extractToken(exchange: ServerWebExchange): Mono<Authentication> {
        val requestPath = exchange.request.path.toString()
        val externallyExposedEndpointType = securityProperties.getExternallyExposedEndpointType(requestPath)

        return if (externallyExposedEndpointType != null) {
            val authToken = exchange.request.headers.getFirst(_tokenHeaderName).orEmpty()
            Mono.just(
                ExternalSystemAuthenticationToken(
                    token = authToken,
                    principal = "",
                    credentials = "",
                    requestUrl = requestPath,
                    externalSystemType = externallyExposedEndpointType
                )
            ).logOnSuccess(message = "Extracted external system token") as Mono<Authentication>
        }  else {
            val authToken = exchange.request.headers.getFirst(_tokenHeaderName).orEmpty()
            Mono.just(AuthenticationToken(authToken, "", "", requestPath)) as Mono<Authentication>
        }
    }
}
