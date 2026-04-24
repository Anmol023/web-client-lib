package com.anmol.web_client_lib.security

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.server.PathContainer
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser

@ConfigurationProperties(prefix = "axis.security")
class SecurityProperties {
    var unauthenticatedEndpoints: Array<String> = arrayOf("")
    var externallyExposedEndpoints: Map<ExternalSystemType, Map<ExternallyExposedAuthenticated, Array<String>>> = emptyMap()
    private var patterns: Map<ExternalSystemType, List<PathPattern>> = emptyMap()
    @PostConstruct
    fun initPatterns() {
        val pathPatternParser = PathPatternParser()
        this.patterns = externallyExposedEndpoints.map { entry ->
            entry.key to (entry.value.flatMap { it.value.map { path -> pathPatternParser.parse(path) } })
        }.toMap()
    }

    fun getExternallyExposedEndpointType(url: String): ExternalSystemType? {
        patterns.forEach { entry ->
            if (entry.value.any { it.matches(PathContainer.parsePath(url)) }) return entry.key
        }
        return null
    }
}
