package com.anmol.web_client_lib.expection_handling

open class BadDataException(
    errorCode: String,
    message: String,
    details: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
) : BaseException(errorCode, message, details, cause) {
    constructor(serviceError: ServiceError, details: Map<String, Any> = emptyMap(), cause: Throwable? = null) : this(
        serviceError.errorCode,
        serviceError.message,
        details,
        cause
    )

    constructor(errorResponse: ErrorResponse, cause: Throwable? = null) : this(
        errorResponse.errorCode,
        errorResponse.message,
        errorResponse.details,
        cause
    )
}

open class DataNotFound(
    errorCode: String,
    message: String,
    details: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
) : BaseException(errorCode, message, details, cause) {
    constructor(serviceError: ServiceError, details: Map<String, Any> = emptyMap(), cause: Throwable? = null) : this(
        serviceError.errorCode,
        serviceError.message,
        details,
        cause
    )

    constructor(errorResponse: ErrorResponse, cause: Throwable? = null) : this(
        errorResponse.errorCode,
        errorResponse.message,
        errorResponse.details,
        cause
    )
}

open class DataNotFoundESB(
    errorCode: String,
    message: String,
    details: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
) : BaseException(errorCode, message, details, cause) {
    constructor(serviceError: ServiceError, details: Map<String, Any> = emptyMap(), cause: Throwable? = null) : this(
        serviceError.errorCode,
        serviceError.message,
        details,
        cause
    )

    constructor(errorResponse: ErrorResponse, cause: Throwable? = null) : this(
        errorResponse.errorCode,
        errorResponse.message,
        errorResponse.details,
        cause
    )
}

open class UnprocessableEntityException(
    errorCode: String,
    message: String,
    details: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
) : BaseException(errorCode, message, details, cause) {
    constructor(serviceError: ServiceError, details: Map<String, Any> = emptyMap(), cause: Throwable? = null) : this(
        serviceError.errorCode,
        serviceError.message,
        details,
        cause
    )

    constructor(errorResponse: ErrorResponse, cause: Throwable? = null) : this(
        errorResponse.errorCode,
        errorResponse.message,
        errorResponse.details,
        cause
    )
}

open class UnauthorizedException(
    errorCode: String,
    message: String,
    details: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
) : BaseException(errorCode, message, details, cause) {
    constructor(serviceError: ServiceError, details: Map<String, Any> = emptyMap(), cause: Throwable? = null) : this(
        serviceError.errorCode,
        serviceError.message,
        details,
        cause
    )

    constructor(errorResponse: ErrorResponse, cause: Throwable? = null) : this(
        errorResponse.errorCode,
        errorResponse.message,
        errorResponse.details,
        cause
    )
}

open class ForbiddenException(
    errorCode: String,
    message: String,
    details: Map<String, Any> = emptyMap(),
    cause: Throwable? = null
) : BaseException(errorCode, message, details, cause) {
    constructor(serviceError: ServiceError, details: Map<String, Any> = emptyMap(), cause: Throwable? = null) : this(
        serviceError.errorCode,
        serviceError.message,
        details,
        cause
    )

    constructor(errorResponse: ErrorResponse, cause: Throwable? = null) : this(
        errorResponse.errorCode,
        errorResponse.message,
        errorResponse.details,
        cause
    )
}

