package dev.betrix.superSmashMobsBrawl

import com.github.shynixn.mccoroutine.bukkit.launch
import gg.flyte.twilight.environment.Environment
import gg.flyte.twilight.scheduler.repeatingTask
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.logging.Handler
import java.util.logging.LogRecord

@Serializable
data class AxiomLogEvent(
    val timestamp: String,
    val level: String,
    val logger: String,
    val message: String,
    val thread: String,
    val thrown: String? = null
)

class AxiomLoggerHandler(private val plugin: SuperSmashMobsBrawl) : Handler() {

    private val axiomApiToken = System.getenv("AXIOM_API_TOKEN")
    private val axiomDatasetName = System.getenv("AXIOM_DATASET_NAME")

    private val queue = ConcurrentLinkedQueue<AxiomLogEvent>()

    private val axiomApiClient = HttpClient(CIO) {
        defaultRequest {
            header("Authorization", "Bearer $axiomApiToken")
            url("https://api.axiom.co/v1/datasets/$axiomDatasetName")
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    init {
        require(axiomApiToken.isNotBlank()) {
            "AXIOM_API_TOKEN was not set"
        }

        require(axiomDatasetName.isNotBlank()) {
            "AXIOM_DATASET_NAME was not set"
        }

        repeatingTask(20 * 5, async = true) {
            flushQueue()
        }
    }

    override fun publish(record: LogRecord?) {
        println("new record type shit")
        println(record)
        if (record == null) {
            return
        }

        val event = AxiomLogEvent(
            timestamp = Instant.ofEpochMilli(record.millis).toString(),
            level = record.level.name.let {
                when (it.lowercase()) {
                    "severe" -> "error"
                    else -> it
                }
            },
            logger = record.loggerName ?: "unknown",
            message = record.message ?: "",
            thread = Thread.currentThread().name,
            thrown = record.thrown?.stackTraceToString()
        )

        queue.add(event)
    }

    override fun flush() {
        flushQueue()
    }

    override fun close() {
        flushQueue()
        axiomApiClient.close()
    }

    private fun flushQueue() {
        if (queue.isEmpty()) return

        val batch = mutableListOf<AxiomLogEvent>()
        while (true) {
            val log = queue.poll() ?: break
            batch.add(log)
        }

        if (batch.isEmpty()) return

        plugin.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = axiomApiClient.post("https://api.axiom.co/v1/datasets/$axiomDatasetName/ingest") {
                        setBody(batch)
                    }

                    if (!response.status.isSuccess()) {
                        throw RuntimeException("Error ingesting log data to Axiom, status: ${response.status.toString()}, response body: ${response.bodyAsText()}")
                    }
                } catch (e: Exception) {
                    plugin.logger.severe("Failed to send logs to Axiom: ${e.message}")
                }
            }
        }
    }
}