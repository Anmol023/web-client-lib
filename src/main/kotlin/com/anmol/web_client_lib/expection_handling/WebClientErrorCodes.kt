package com.anmol.web_client_lib.expection_handling



enum class WebClientError(override val errorCode: String, override val message: String) : ServiceError {
    WEB1501("AUTH_VALIDATION_ERROR", "Authentication validation failed")
}
