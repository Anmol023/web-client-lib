package com.anmol.web_client_lib.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@Component
@ConditionalOnMissingBean(TokenServiceLocal::class)
class TokenService(
    @Autowired private val webClient: WebClient,
    @Value("\${web_client.token-service.validation-url}") val tokenValidationUrl: String
) {
    fun validate(token: String, requestUrl: String): Mono<Boolean> {
        return webClient
            .post()
            .uri(tokenValidationUrl)
            .bodyValue(RequestToken(token, requestUrl))
            .retrieve()
            .bodyToMono(mutableMapOf<String, Boolean>()::class.java)
            .map { it["valid"] == true }
            .onErrorReturn(false)
    }
}

@Profile(*["local", "test"])
@Component
class TokenServiceLocal : TokenService(WebClient.create(), "") {
    override fun validate(token: String, requestUrl: String): Mono<Boolean> {
        return Mono.just(true)
    }

}