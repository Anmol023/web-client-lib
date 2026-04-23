package com.example.web_client_lib.logging

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ServiceDetails(
    @Value("\${info.app.name}") serviceName: String
){
    init {
        Companion.serviceName = serviceName
    }
    companion object {
        private lateinit var serviceName: String

        fun getServiceName(): String {
            return if (this::serviceName.isInitialized) serviceName else ""
        }
    }
}