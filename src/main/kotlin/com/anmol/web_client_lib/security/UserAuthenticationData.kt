package com.anmol.web_client_lib.security



interface IAuthenticationData

open class UserAuthenticationData(
    open val id: String,
    open val loginMetadata: Map<String, Any> = emptyMap()
): IAuthenticationData

data class ExternalAuthenticationData(
    val sourceChannel: String
): IAuthenticationData

data class CustomerAuthenticationData(
    override val id: String,
    override val loginMetadata: Map<String, Any> = emptyMap(),
    val role: Role = Role.CUSTOMER
): UserAuthenticationData(id, loginMetadata)


enum class Role(val value:String){
    CUSTOMER("customer"),
    VENDOR("vendor"),
    ADMIN("admin");

    companion object {
        fun fromString(value: String?) = Role.entries.find{ it.value == value}?: throw IllegalArgumentException("Invalid role value: $value")
    }
}

data class TokenValidationResponse(
    val valid: Boolean = false,
    val claims: Claims? = null
)

data class Claims(
    val mobileNumber: String? = null,
    val role: String? = null,
    val loginMetadata: Map<String, Any>? = null
)