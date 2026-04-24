package com.anmol.web_client_lib.logging

import com.anmol.web_client_lib.serializer.DefaultSerializer.serialize
import org.slf4j.LoggerFactory

class Logger(className: Class<out Any>) {
    private val logger = LoggerFactory.getLogger(className)

    fun info(details: LogDetails) {
//        val encryptedDetails =  Add encryption to encrypt logs
        logger.info(serialize(details.copy(additionalDetails = details.additionalDetails)))
    }

    fun error(details: LogDetails, exception: Throwable) {
//        val encryptedDetails =  Add encryption to encrypt logs
        logger.error(serialize(details.copy(additionalDetails = details.additionalDetails)), exception)
    }
}
