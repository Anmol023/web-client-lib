package com.anmol.web_client_lib.logging

import org.springframework.http.HttpMethod
import tools.jackson.databind.annotation.JsonSerialize

class RequestDetails(
    @field: JsonSerialize(using = HttpMethodSerializer::class)
    val requestMethod: HttpMethod,
    val requestHeaders: Map<String, Any>? = null,
    var uriWithParams: String? = null,
    val requestBody: String? = null
)