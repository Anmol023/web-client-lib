package com.anmol.web_client_lib.serializer

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.util.StdDateFormat
import tools.jackson.module.kotlin.kotlinModule

object ObjectMapperCache {

    val objectMapperCache: ObjectMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .defaultDateFormat(StdDateFormat().withColonInTimeZone(true))
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build()
}