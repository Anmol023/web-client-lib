package com.anmol.web_client_lib.serializer

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.util.StdDateFormat
import tools.jackson.module.kotlin.kotlinModule


internal object DefaultSerializer {
    private val objectMapperExcludeNull: JsonMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .defaultDateFormat(StdDateFormat().withColonInTimeZone(true))
        .changeDefaultPropertyInclusion{ it.withValueInclusion(JsonInclude.Include.NON_NULL) }
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build()

    fun serialize(obj: Any?): String {
        return objectMapperExcludeNull.writeValueAsString(obj)
    }

    fun <T> deserialize(str: String, type: Class<T>): T {
        return objectMapperExcludeNull.readValue(str, type)
    }
}