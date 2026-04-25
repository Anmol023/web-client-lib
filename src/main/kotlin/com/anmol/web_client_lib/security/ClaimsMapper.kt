package com.anmol.web_client_lib.security

import org.springframework.stereotype.Component

fun interface ClaimsMapper {
    fun map(claims: Map<String, Any>): CustomerAuthenticationData
}

@Component
class DefaultClaimsMapper : ClaimsMapper {
    override fun map(claims: Map<String, Any>): CustomerAuthenticationData {
        val mobileNumber = claims["mobileNumber"] as? String ?: throw IllegalArgumentException("Missing mobileNumber")
        val roleStr = claims["role"] as? String ?: throw IllegalArgumentException("Missing role")
        val role = Role.fromString(roleStr)
        val loginMetadata = claims["loginMetadata"] as? Map<String, Any> ?: emptyMap()
        return CustomerAuthenticationData(id = mobileNumber, loginMetadata = loginMetadata, role = role)
    }
}
