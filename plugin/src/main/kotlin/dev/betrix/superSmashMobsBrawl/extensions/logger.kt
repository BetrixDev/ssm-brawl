package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.AxiomLoggerHandler
import java.util.concurrent.ConcurrentHashMap
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

internal val placeholderRegex = Regex("\\{(\\d+|\\w+)\\}")

private fun Any?.toDisplayString(): String {
    return when (this) {
        null -> "null"
        is Throwable ->
            this.message?.let { "${this::class.simpleName ?: "Throwable"}: $it" } ?: this.toString()
        is Array<*> -> this.contentToString()
        is IntArray -> this.contentToString()
        is LongArray -> this.contentToString()
        is DoubleArray -> this.contentToString()
        is FloatArray -> this.contentToString()
        is ShortArray -> this.contentToString()
        is ByteArray -> this.contentToString()
        is BooleanArray -> this.contentToString()
        is Iterable<*> ->
            this.joinToString(", ", prefix = "[", postfix = "]") { it?.toString() ?: "null" }
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

internal data class FormattedMessage(
    val formatted: String,
    val properties: ConcurrentHashMap<String, JsonElement>,
)

internal fun formatMessage(template: String, args: Array<out Any?>): FormattedMessage {
    val props = ConcurrentHashMap<String, JsonElement>()
    var namedArgIndex = 0

    val formatted =
        placeholderRegex.replace(template) { matchResult ->
            val placeholder = matchResult.groupValues[1]

            val replacement: String =
                if (placeholder.matches(Regex("\\d+"))) {
                    // Indexed placeholder (e.g., {0}, {1})
                    val index = placeholder.toInt()
                    if (index < args.size) {
                        val value = args[index]
                        // Use the index as the property name for consistency
                        props.putIfAbsent(index.toString(), value.toJsonElement())
                        value.toDisplayString()
                    } else {
                        matchResult.value // Keep placeholder if index is out of bounds
                    }
                } else {
                    // Named placeholder (e.g., {name})
                    // Consume arguments sequentially starting from 0
                    if (namedArgIndex < args.size) {
                        val value = args[namedArgIndex]
                        props[placeholder] = value.toJsonElement()
                        namedArgIndex++
                        value.toDisplayString()
                    } else {
                        matchResult.value // Keep placeholder if no more args
                    }
                }
            replacement
        }

    return FormattedMessage(formatted = formatted, properties = props)
}

private fun buildLogJson(
    level: String,
    formatted: String,
    template: String,
    properties: ConcurrentHashMap<String, JsonElement>,
    exception: Throwable? = null,
): JsonElement = buildJsonObject {
    put("level", level)
    put("message", formatted)
    put("messageTemplate", template)
    if (properties.isNotEmpty()) {
        putJsonObject("properties") { properties.forEach { (k, v) -> put(k, v) } }
    }
    exception?.let { put("exception", it.stackTraceToString()) }
}

fun Logger.info(template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.info(formatted)
    this.logJson(buildLogJson("info", formatted, template, properties))
}

fun Logger.warn(template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.warning(formatted)
    this.logJson(buildLogJson("warn", formatted, template, properties))
}

fun Logger.error(template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.severe(formatted)
    this.logJson(buildLogJson("error", formatted, template, properties))
}

fun Logger.info(ex: Throwable, template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.log(Level.INFO, formatted, ex)
    this.logJson(buildLogJson("info", formatted, template, properties, ex))
}

fun Logger.warn(ex: Throwable, template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.log(Level.WARNING, formatted, ex)
    this.logJson(buildLogJson("warn", formatted, template, properties, ex))
}

fun Logger.error(ex: Throwable, template: String, vararg args: Any?) {
    val (formatted, properties) = formatMessage(template, args)
    this.log(Level.SEVERE, formatted, ex)
    this.logJson(buildLogJson("error", formatted, template, properties, ex))
}
