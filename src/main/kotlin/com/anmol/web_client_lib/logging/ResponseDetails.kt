package com.anmol.web_client_lib.logging

class ResponseDetails(
    val responseCode: String? = null,
    val responseHeaders: Map<String, Any>? = null,
    val responseStatus: String = "",
    val responseTime: Long = -1,
    val responseBody: String? = null
)