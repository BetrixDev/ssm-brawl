package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.AxiomLoggerHandler
import java.util.logging.Level
import java.util.logging.Logger
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

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

private val placeholderRegex = Regex("\\{([^{}]+)\\}")

private fun Any?.toDisplayString(): String {
    return when (this) {
        null -> "null"
        is Throwable -> this.message?.let { "${this::class.simpleName}: $it" } ?: this.toString()
        is Array<*> -> this.contentToString()
        is IntArray -> this.contentToString()
        is LongArray -> this.contentToString()
        is DoubleArray -> this.contentToString()
        is FloatArray -> this.contentToString()
        is ShortArray -> this.contentToString()
        is ByteArray -> this.contentToString()
        is BooleanArray -> this.contentToString()
        is Iterable<*> -> this.joinToString(", ") { it.toDisplayString() }
        else -> this.toString()
    }
}

private fun Any?.toJsonElement(): JsonElement {
    return when (this) {
        null -> JsonPrimitive(null as String?)
        is JsonElement -> this
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        is Throwable -> JsonPrimitive(this.stackTraceToString())
        else -> JsonPrimitive(this.toString())
    }
}

private data class FormattedMessage(
    val formatted: String,
    val properties: LinkedHashMap<String, JsonElement>,
)

private fun formatMessage(template: String, args: Array<out Any?>): FormattedMessage {
    var argIndex = 0
    val props = LinkedHashMap<String, JsonElement>()

    val formatted =
        placeholderRegex.replace(template) { matchResult ->
            val name = matchResult.groupValues[1]
            val replacement: String =
                if (argIndex < args.size) {
                    val value = args[argIndex++]
                    // Keep first occurrence if duplicate property names appear
                    props.putIfAbsent(name, value.toJsonElement())
                    value.toDisplayString()
                } else {
                    matchResult.value
                }
            replacement
        }

    return FormattedMessage(formatted = formatted, properties = props)
}

fun Logger.info(template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.info(formatted)
    this.logJson(
        buildJsonObject {
            put("level", "info")
            put("message", formatted)
            put("messageTemplate", template)
            if (properties.isNotEmpty()) {
                putJsonObject("properties") { properties.forEach { (k, v) -> put(k, v) } }
            }
        }
    )
}

fun Logger.warn(template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.warning(formatted)
    this.logJson(
        buildJsonObject {
            put("level", "warn")
            put("message", formatted)
            put("messageTemplate", template)
            if (properties.isNotEmpty()) {
                putJsonObject("properties") { properties.forEach { (k, v) -> put(k, v) } }
            }
        }
    )
}

fun Logger.error(template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.severe(formatted)
    this.logJson(
        buildJsonObject {
            put("level", "error")
            put("message", formatted)
            put("messageTemplate", template)
            if (properties.isNotEmpty()) {
                putJsonObject("properties") { properties.forEach { (k, v) -> put(k, v) } }
            }
        }
    )
}

fun Logger.info(ex: Throwable, template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.log(Level.INFO, formatted, ex)
    this.logJson(
        buildJsonObject {
            put("level", "info")
            put("message", formatted)
            put("messageTemplate", template)
            if (properties.isNotEmpty()) {
                putJsonObject("properties") { properties.forEach { (k, v) -> put(k, v) } }
            }
            put("exception", ex.stackTraceToString())
        }
    )
}

fun Logger.warn(ex: Throwable, template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.log(Level.WARNING, formatted, ex)
    this.logJson(
        buildJsonObject {
            put("level", "warn")
            put("message", formatted)
            put("messageTemplate", template)
            if (properties.isNotEmpty()) {
                putJsonObject("properties") { properties.forEach { (k, v) -> put(k, v) } }
            }
            put("exception", ex.stackTraceToString())
        }
    )
}

fun Logger.error(ex: Throwable, template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.log(Level.SEVERE, formatted, ex)
    this.logJson(
        buildJsonObject {
            put("level", "error")
            put("message", formatted)
            put("messageTemplate", template)
            if (properties.isNotEmpty()) {
                putJsonObject("properties") { properties.forEach { (k, v) -> put(k, v) } }
            }
            put("exception", ex.stackTraceToString())
        }
    )
}
