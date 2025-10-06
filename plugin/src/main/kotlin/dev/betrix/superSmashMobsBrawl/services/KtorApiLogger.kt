package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.AxiomLoggerHandler
import io.ktor.client.plugins.api.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.serialization.json.*
import java.time.Instant
import java.util.UUID

/**
 * Custom Ktor API logger plugin that logs detailed API request/response information to Axiom.
 * 
 * Tracks comprehensive datapoints for creating detailed dashboards including:
 * - Request/response metadata (method, URL, status, headers)
 * - Timing information (duration, timestamps)
 * - Payload sizes
 * - Error tracking
 * - Correlation IDs for request tracing
 */
val KtorApiLogger = createClientPlugin("KtorApiLogger", ::KtorApiLoggerConfig) {
    val axiomHandler = pluginConfig.axiomHandler
    val sanitizedHeaders = pluginConfig.sanitizedHeaders

    on(Send) { request ->
        val requestId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        val startInstant = Instant.now()

        // Capture request details
        val requestMethod = request.method.value
        val requestUrl = request.url.buildString()
        val requestHeaders = captureHeadersFromBuilder(request.headers, sanitizedHeaders)
        val requestContentType = request.contentType()?.toString()
        val requestBodySize = "unknown"

        var responseStatus: Int? = null
        var responseTime: Long? = null
        var responseBodySize: Long? = null
        var errorMessage: String? = null
        var errorType: String? = null

        try {
            // Execute the request
            val call = proceed(request)
            
            // Capture response details
            responseStatus = call.response.status.value
            responseTime = System.currentTimeMillis() - startTime
            
            // Try to capture response body size
            responseBodySize = try {
                call.response.contentLength()
            } catch (e: Exception) {
                null
            }

            // Log successful request
            logApiRequest(
                axiomHandler = axiomHandler,
                requestId = requestId,
                timestamp = startInstant.toString(),
                method = requestMethod,
                url = requestUrl,
                requestHeaders = requestHeaders,
                requestContentType = requestContentType,
                requestBodySize = requestBodySize,
                responseStatus = responseStatus,
                responseTime = responseTime,
                responseBodySize = responseBodySize,
                success = call.response.status.isSuccess(),
                errorMessage = null,
                errorType = null
            )

            call
        } catch (e: Exception) {
            // Capture error details
            responseTime = System.currentTimeMillis() - startTime
            errorMessage = e.message
            errorType = e.javaClass.simpleName

            // Log failed request
            logApiRequest(
                axiomHandler = axiomHandler,
                requestId = requestId,
                timestamp = startInstant.toString(),
                method = requestMethod,
                url = requestUrl,
                requestHeaders = requestHeaders,
                requestContentType = requestContentType,
                requestBodySize = requestBodySize,
                responseStatus = responseStatus,
                responseTime = responseTime,
                responseBodySize = responseBodySize,
                success = false,
                errorMessage = errorMessage,
                errorType = errorType
            )

            throw e
        }
    }
}

class KtorApiLoggerConfig {
    lateinit var axiomHandler: AxiomLoggerHandler
    var sanitizedHeaders: Set<String> = setOf("Authorization", "X-API-Key", "Cookie")
}

/**
 * Captures headers from HeadersBuilder for logging, sanitizing sensitive ones
 */
private fun captureHeadersFromBuilder(headersBuilder: HeadersBuilder, sanitizedHeaders: Set<String>): Map<String, String> {
    val headers = headersBuilder.build()
    return headers.entries()
        .associate { (key, values) ->
            val value = if (sanitizedHeaders.any { it.equals(key, ignoreCase = true) }) {
                "***REDACTED***"
            } else {
                values.joinToString(", ")
            }
            key to value
        }
}

/**
 * Logs API request details to Axiom with comprehensive datapoints
 */
private fun logApiRequest(
    axiomHandler: AxiomLoggerHandler,
    requestId: String,
    timestamp: String,
    method: String,
    url: String,
    requestHeaders: Map<String, String>,
    requestContentType: String?,
    requestBodySize: String,
    responseStatus: Int?,
    responseTime: Long?,
    responseBodySize: Long?,
    success: Boolean,
    errorMessage: String?,
    errorType: String?
) {
    val logData = buildJsonObject {
        // Core identification
        put("_time", JsonPrimitive(timestamp))
        put("logger", JsonPrimitive("ktor-api-client"))
        put("request_id", JsonPrimitive(requestId))
        
        // Request details
        putJsonObject("request") {
            put("method", JsonPrimitive(method))
            put("url", JsonPrimitive(url))
            put("content_type", JsonPrimitive(requestContentType ?: "unknown"))
            put("body_size", JsonPrimitive(requestBodySize))
            
            // Parse URL components for better filtering
            val urlObj = try {
                Url(url)
            } catch (e: Exception) {
                null
            }
            
            if (urlObj != null) {
                put("host", JsonPrimitive(urlObj.host))
                put("path", JsonPrimitive(urlObj.encodedPath))
                put("protocol", JsonPrimitive(urlObj.protocol.name))
                put("port", JsonPrimitive(urlObj.port))
                
                // Query parameters (if any)
                if (urlObj.parameters.names().isNotEmpty()) {
                    putJsonObject("query_params") {
                        urlObj.parameters.names().forEach { name ->
                            put(name, JsonPrimitive(urlObj.parameters[name]))
                        }
                    }
                }
            }
            
            // Headers (sanitized)
            putJsonObject("headers") {
                requestHeaders.forEach { (key, value) ->
                    put(key, JsonPrimitive(value))
                }
            }
        }
        
        // Response details
        putJsonObject("response") {
            put("status", JsonPrimitive(responseStatus))
            put("body_size", JsonPrimitive(responseBodySize))
            put("success", JsonPrimitive(success))
            
            // Response status category for easier filtering
            responseStatus?.let { status ->
                put("status_category", JsonPrimitive(when (status) {
                    in 100..199 -> "informational"
                    in 200..299 -> "success"
                    in 300..399 -> "redirection"
                    in 400..499 -> "client_error"
                    in 500..599 -> "server_error"
                    else -> "unknown"
                }))
            }
        }
        
        // Timing information
        putJsonObject("timing") {
            put("duration_ms", JsonPrimitive(responseTime))
            
            // Performance categorization
            responseTime?.let { duration ->
                put("performance_category", JsonPrimitive(when {
                    duration < 100 -> "fast"
                    duration < 500 -> "normal"
                    duration < 1000 -> "slow"
                    else -> "very_slow"
                }))
            }
        }
        
        // Error details (if applicable)
        if (!success || errorMessage != null) {
            putJsonObject("error") {
                put("has_error", JsonPrimitive(true))
                put("message", JsonPrimitive(errorMessage ?: "unknown"))
                put("type", JsonPrimitive(errorType ?: "unknown"))
            }
        } else {
            putJsonObject("error") {
                put("has_error", JsonPrimitive(false))
            }
        }
        
        // Metadata for context
        putJsonObject("metadata") {
            put("environment", JsonPrimitive(System.getenv("ENVIRONMENT") ?: "unknown"))
            put("thread", JsonPrimitive(Thread.currentThread().name))
            put("service", JsonPrimitive("minecraft-plugin"))
        }
    }

    axiomHandler.logJson(logData, "ktor-api-client")
}
