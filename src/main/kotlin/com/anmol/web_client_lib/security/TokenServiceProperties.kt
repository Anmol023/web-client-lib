package com.anmol.web_client_lib.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "web-client-lib.token-service")
class TokenServiceProperties {
    lateinit var validationUrl: String
    lateinit var externalTokenValidationUrl: String
    var requiredClaims: List<String> = listOf("mobileNumber", "role")
}

