package com.anmol.web_client_lib.security.config

import org.springframework.boot.http.codec.autoconfigure.HttpCodecsProperties
import org.springframework.util.unit.DataSize

/**
 * This method returns default maxInMemorySize for client codes.
 * Any service can opt to configure "spring.codec.max-in-memory-size" property.
 * If configured then configured value will be used.
 * Else default value of 256 will be configured.
 */
fun HttpCodecsProperties.getMaxInMemorySizeWithDefault() =
    this.maxInMemorySize ?: DataSize.ofKilobytes(256)
