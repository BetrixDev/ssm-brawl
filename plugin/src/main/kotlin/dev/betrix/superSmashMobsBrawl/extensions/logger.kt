package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.AxiomLoggerHandler
import java.util.logging.Logger
import kotlinx.serialization.json.JsonElement

fun Logger.logJson(json: JsonElement) {
    var current: Logger? = this
    while (current != null) {
        for (handler in current.handlers) {
            if (handler is AxiomLoggerHandler) {
                handler.logJson(json, loggerName = this.name)
            }
        }
        current = if (current.useParentHandlers) current.parent else null
    }
}
