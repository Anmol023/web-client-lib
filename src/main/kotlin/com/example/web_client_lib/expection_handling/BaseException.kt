package com.example.web_client_lib.expection_handling

open class BaseException(
    val errorCode: String,
    override val message: String,
    var details: Map<String, Any> = emptyMap(),
    override val cause: Throwable? = null
) : Throwable(message = message, cause = cause) {
    constructor(serviceError: ServiceError, details: Map<String, Any> = emptyMap(), cause: Throwable? = null) : this(serviceError.errorCode, serviceError.message, details, cause)
    constructor(errorResponse: ErrorResponse, cause: Throwable? = null) : this(errorResponse.errorCode, errorResponse.message, errorResponse.details, cause)

    fun toMap(): Map<String, Any> {
        return mapOf(
            "errorCode" to errorCode,
            "message" to message,
            "details" to details
        )
    }
}