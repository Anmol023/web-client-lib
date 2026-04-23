package com.example.web_client_lib.serializer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

internal object DefaultSerializer {
    private val objectMapperExcludeNull: ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .setDateFormat(StdDateFormat().withColonInTimeZone(true))
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    fun serialize(obj: Any?): String {
        return objectMapperExcludeNull.writeValueAsString(obj)
    }

    fun <T> deserialize(str: String, type: Class<T>): T {
        return objectMapperExcludeNull.readValue(str, type)
    }
}