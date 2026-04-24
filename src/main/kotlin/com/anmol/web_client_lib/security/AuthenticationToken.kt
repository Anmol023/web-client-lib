package com.anmol.web_client_lib.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class AuthenticationToken(
    val token: String, principal: String = "", credentials: String = "",
    val requestUrl: String
) : UsernamePasswordAuthenticationToken(principal, credentials) {
    fun authenticated(): UsernamePasswordAuthenticationToken {
        return UsernamePasswordAuthenticationToken("", "", emptyList())

    }
}

class ExternalSystemAuthenticationToken(
    val token: String,
    principal: String = "",
    credentials: String = "",
    val requestUrl: String,
    val externalSystemType: ExternalSystemType
) : UsernamePasswordAuthenticationToken(principal, credentials) {
    fun authenticated(): UsernamePasswordAuthenticationToken {
        return UsernamePasswordAuthenticationToken("", "", emptyList())
    }
}

enum class ExternalSystemType {
    external, // the urls which are exposed to external systems and requires authentication
    internal // if you have some internal systems which are calling your service, and you want to do authentication and authorization for those, you can use this type to differentiate between external and internal calls
}

enum class ExternallyExposedAuthenticated {
    exposedAndAuthenticated, // the urls which are exposed to external systems and requires authentication
    authenticated // internal requests which comes as part of above, required to do authorization
}