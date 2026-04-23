package com.example.web_client_lib.expection_handling

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ErrorResponse(val message: String, val errorCode: String, val details: Map<String, Any> = emptyMap()) {
    constructor() : this("GENERIC_ERROR", "Could not get error message")
    constructor(serviceError: ServiceError) : this(serviceError.message, serviceError.errorCode)

    fun toServiceError(): ServiceError {
        val msg = message
        val errorcode = errorCode
        return object : ServiceError {
            override val errorCode: String
                get() = errorcode
            override val message: String
                get() = msg
        }
    }
}