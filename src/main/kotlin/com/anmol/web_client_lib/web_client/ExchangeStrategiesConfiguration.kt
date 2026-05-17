package com.anmol.web_client_lib.web_client

import com.anmol.web_client_lib.security.config.getMaxInMemorySizeWithDefault
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.http.codec.autoconfigure.HttpCodecsProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies

@Configuration
@ComponentScan("com.anmol.web_client_lib.web_client")
@EnableConfigurationProperties(HttpCodecsProperties::class)
class ExchangeStrategiesConfiguration {

    @Bean
    fun exchangeStrategies(codecProperties: HttpCodecsProperties) : ExchangeStrategies {
        return ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(codecProperties.getMaxInMemorySizeWithDefault().toBytes().toInt())
            }
            .build()
    }

}
