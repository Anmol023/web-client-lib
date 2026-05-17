package com.axis.thanos.config

import org.springframework.boot.autoconfigure.codec.CodecProperties
import org.springframework.util.unit.DataSize

/**
 * This method returns default maxInMemorySize for client codes.
 * Any service can opt to configure "spring.codec.max-in-memory-size" property.
 * If configured then configured value will be used.
 * Else default value of 256 will be configured.
 */
fun CodecProperties.getMaxInMemorySizeWithDefault() =
    if (this.maxInMemorySize == null) DataSize.ofKilobytes(256) else this.maxInMemorySize
