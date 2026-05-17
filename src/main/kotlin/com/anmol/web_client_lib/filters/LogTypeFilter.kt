package com.anmol.web_client_lib.filters

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.AbstractMatcherFilter
import ch.qos.logback.core.spi.FilterReply

class LogTypeFilter : AbstractMatcherFilter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        return if (event.message.contains("logType", true)) {
            onMatch
        } else {
            onMismatch
        }
    }
}
