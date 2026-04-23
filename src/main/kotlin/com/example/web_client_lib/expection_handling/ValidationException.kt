package com.example.web_client_lib.expection_handling

import com.fasterxml.jackson.annotation.JsonProperty

open class ValidationException(
    val validationErrors: ValidationErrorDetails,
    override val message: String
) : Throwable(message = message) {

    val errorCodes = validationErrors.errors.joinToString(separator = ", ") { it.errorCode }
}

class ValidationErrorDetails(@JsonProperty("errors") val errors: List<ErrorResponse>) {
    constructor(error: ErrorResponse) : this(listOf(error))
}
