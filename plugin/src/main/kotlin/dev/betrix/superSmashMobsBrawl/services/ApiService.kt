package dev.betrix.superSmashMobsBrawl.services

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.AxiomLoggerHandler
import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.extensions.ticks
import dev.betrix.superSmashMobsBrawl.models.player.PlayerDocument
import dev.betrix.superSmashMobsBrawl.utils.DockerDetector
import gg.flyte.twilight.scheduler.repeatingTask
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.minutes

object ApiService : IManageable, KoinComponent {
    private val axiomLoggerHandler: AxiomLoggerHandler by inject()
    private val plugin: SuperSmashMobsBrawl by inject()

    private val isDocker = DockerDetector.isRunningInDocker()

    private val apiSecretKey = run {
        if (isDocker) {
            System.getenv("API_SECRET_KEY")
        } else {
            "changeme"
        }
    }

    private val baseApiUrl = run {
        if (isDocker) {
            "http://api:3000"
        } else {
            "http://localhost:3000"
        }
    }

    val apiClient =
        HttpClient(CIO) {
            defaultRequest {
                if (!apiSecretKey.isNullOrBlank()) {
                    header("Authorization", "Bearer $apiSecretKey")
                }
                contentType(ContentType.Application.Json)
                url(baseApiUrl)
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
            install(KtorApiLogger) {
                axiomHandler = axiomLoggerHandler
                sanitizedHeaders = setOf("Authorization", "Bearer", "X-API-Key")
            }
        }

    private val apiHealthCheckJob = repeatingTask(1.minutes.ticks) {
        plugin.launch {
            withContext(Dispatchers.IO) {
                doApiHealthCheck()
            }
        }
    }

    override fun setup() {
        runBlocking {
            doApiHealthCheck()
        }
    }

    override fun teardown() {
        apiHealthCheckJob.cancel()
        apiClient.close()
    }

    suspend fun playersGetDocumentAsync(
        player: Player,
        isJoinEvent: Boolean = false,
    ): PlayerDocument {
        val response: PlayerDocument =
            apiClient.get("players/${player.uniqueId}/document?joinEvent=${isJoinEvent}").body()

        return response
    }

    suspend fun playersSetDocumentAsync(player: Player, document: PlayerDocument) {
        apiClient.put("players/${player.uniqueId}/document") {
            contentType(ContentType.Application.Json)
            setBody(document)
        }
    }

    private suspend fun doApiHealthCheck() {
        val response = apiClient.get("/hc")

        if (!response.status.isSuccess()) {
            if (isDocker) {
                throw RuntimeException(
                    "API health check failed with status ${response.status.value}. " +
                            "Ensure the API container is running and accessible.",
                )
            }

            throw RuntimeException("API health check failed with status ${response.status.value}. " +
                    "If you're running the server locally, ensure the API is running at $baseApiUrl.")
        }

        plugin.logger.info("API health check successful.")
    }
}
