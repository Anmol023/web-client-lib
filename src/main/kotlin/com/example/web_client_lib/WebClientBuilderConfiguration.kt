package com.example.web_client_lib

import io.netty.channel.ChannelOption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientBuilderConfiguration {

    private final val connectionProvider = ConnectionProvider.builder("")
        .maxConnections(100)
        .pendingAcquireTimeout(Duration.ofSeconds(20))
        .maxIdleTime(Duration.ofSeconds(20))
        .build()


    val connector = ReactorClientHttpConnector(HttpClient.create(connectionProvider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
        .option(ChannelOption.SO_KEEPALIVE, true)
    )

    @Bean
    @Primary
    fun jsonWebClient(
        headersWebClientFilterFunction: HeadersWebClientFilterFunction,
        exchangeStrategies: ExchangeStrategies,
        webClientLoggingFilter: WebClientLoggingFilter
    ) = WebClient
        .builder()
        .clientConnector(connector)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .filter(webClientLoggingFilter)
        .exchangeStrategies(exchangeStrategies)
        .build()

    @Bean
    @Primary
    fun webClientWrapper(webClient: WebClient) = WebClientWrapper(webClient)

    @Bean
    fun headersWebClientFilterFunction(): HeadersWebClientFilterFunction {
        return HeadersWebClientFilterFunction()
    }

}
