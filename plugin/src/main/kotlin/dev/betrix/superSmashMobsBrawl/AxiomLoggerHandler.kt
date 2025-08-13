package dev.betrix.superSmashMobsBrawl

import com.github.shynixn.mccoroutine.bukkit.launch
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.logging.Handler
import java.util.logging.LogRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class AxiomLogEvent(
    val timestamp: String,
    val level: String,
    val logger: String,
    val message: String,
    val thread: String,
    val thrown: String? = null,
)

class AxiomLoggerHandler(private val plugin: SuperSmashMobsBrawl) : Handler() {

    private val maxBatchSize = 500
    private val axiomApiToken = System.getenv("AXIOM_API_TOKEN")
    private val axiomDatasetName = System.getenv("AXIOM_DATASET_NAME")

    private val queue = ConcurrentLinkedQueue<JsonElement>()

    private var runnable: TwilightRunnable? = null

    private val axiomApiClient =
        HttpClient(CIO) {
            defaultRequest {
                header("Authorization", "Bearer $axiomApiToken")
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = false
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

    init {
        runnable = repeatingTask(20 * 5, async = true) { flushQueue() }
    }

    override fun publish(record: LogRecord?) {
        if (record == null) {
            return
        }

        val event =
            AxiomLogEvent(
                timestamp = Instant.ofEpochMilli(record.millis).toString(),
                level =
                    record.level.name.let {
                        when (it.lowercase()) {
                            "severe" -> "error"
                            else -> it
                        }
                    },
                logger = record.loggerName ?: "unknown",
                message = record.message ?: "",
                thread = Thread.currentThread().name,
                thrown = record.thrown?.stackTraceToString(),
            )

        queue.add(Json.encodeToJsonElement(AxiomLogEvent.serializer(), event))
    }

    fun logJson(value: JsonElement, loggerName: String? = null) {
        val enriched: JsonElement = buildJsonObject {
            put("timestamp", JsonPrimitive(Instant.now().toString()))
            put("logger", JsonPrimitive(loggerName ?: "unknown"))
            put("thread", JsonPrimitive(Thread.currentThread().name))
            when (value) {
                is JsonObject -> value.forEach { (k, v) -> put(k, v) }
                else -> put("data", value)
            }
        }

        // Log in console for observability there as well
        println(value)

        queue.add(enriched)
    }

    override fun flush() {
        flushQueue()
    }

    override fun close() {
        runnable?.cancel()
        flushQueue()
        axiomApiClient.close()
    }

    private fun flushQueue() {
        if (queue.isEmpty()) return

        val batch = mutableListOf<JsonElement>()
        while (batch.size < maxBatchSize) {
            val log = queue.poll() ?: break
            batch.add(log)
        }

        if (batch.isEmpty()) return

        if (axiomApiToken.isBlank() || axiomDatasetName.isBlank()) {
            plugin.logger.info("Axiom values not set in ENV, skipping ingesting logs")
            return
        }

        plugin.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response =
                        axiomApiClient.post(
                            "https://api.axiom.co/v1/datasets/$axiomDatasetName/ingest"
                        ) {
                            setBody(batch)
                        }

                    if (!response.status.isSuccess()) {
                        throw RuntimeException(
                            "Error ingesting log data to Axiom, status: ${response.status.toString()}, response body: ${response.bodyAsText()}"
                        )
                    }
                } catch (e: Exception) {
                    plugin.logger.severe("Failed to send logs to Axiom: ${e.message}")
                }
            }
        }
    }
}
