package com.anmol.web_client_lib.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer

@Configuration
class AuthenticationWebfluxConfigurer(
    @Autowired val authenticationTokenDataResolver: AuthenticationTokenDataResolver
) : WebFluxConfigurer {

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(authenticationTokenDataResolver)
    }

}
