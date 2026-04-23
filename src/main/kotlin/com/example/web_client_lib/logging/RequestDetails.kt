package com.example.web_client_lib.logging

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.springframework.http.HttpMethod

class RequestDetails(
    @field: JsonSerialize(using = HttpMethodSerializer::class)
    val requestMethod: HttpMethod,
    val requestHeaders: Map<String, Any>? = null,
    var uriWithParams: String? = null,
    val requestBody: String? = null
)