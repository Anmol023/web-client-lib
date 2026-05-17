package com.axis.thanos.web_client

import com.axis.thanos.config.getMaxInMemorySizeWithDefault
import org.springframework.boot.autoconfigure.codec.CodecProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies

@Configuration
@ComponentScan("com.axis.thanos.web_client")
@EnableConfigurationProperties(CodecProperties::class)
class ExchangeStrategiesConfiguration {

    @Bean
    fun exchangeStrategies(codecProperties: CodecProperties) : ExchangeStrategies {
        return ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(codecProperties.getMaxInMemorySizeWithDefault().toBytes().toInt())
            }
            .build()
    }

}
