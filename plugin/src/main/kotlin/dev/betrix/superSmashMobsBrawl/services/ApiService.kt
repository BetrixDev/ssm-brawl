package dev.betrix.superSmashMobsBrawl.services

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.AxiomLoggerHandler
import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.extensions.ticks
import dev.betrix.superSmashMobsBrawl.models.ServerStatus
import dev.betrix.superSmashMobsBrawl.models.player.PlayerDocument
import gg.flyte.twilight.scheduler.repeatingTask
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object ApiService : IManageable, KoinComponent {
    private val axiomLoggerHandler: AxiomLoggerHandler by inject()
    private val plugin: SuperSmashMobsBrawl by inject()

    private val apiSecretKey = System.getenv("API_SECRET_KEY") ?: "change-me"

    private val convexSiteUrl = System.getenv("CONVEX_SITE_URL")

    val apiClient =
            HttpClient(CIO) {
                defaultRequest {
                    if (apiSecretKey.isNotBlank()) {
                        header("Authorization", "Bearer $apiSecretKey")
                    }
                    contentType(ContentType.Application.Json)
                    url(convexSiteUrl)
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

    private val apiHealthCheckJob =
            repeatingTask(10.minutes.ticks) {
                plugin.launch { withContext(Dispatchers.IO) { doApiHealthCheck() } }
            }

    override fun setup() {
        runBlocking { doApiHealthCheck() }
    }

    override fun teardown() {
        apiHealthCheckJob.cancel()
        apiClient.close()
    }

    suspend fun playersGetDocumentAsync(
            player: Player,
    ): PlayerDocument {
        val response: PlayerDocument =
            apiClient.get("players/${player.uniqueId}/document?username=${player.name}").body()

        return response
    }

    suspend fun playersSetDocumentAsync(player: Player, document: PlayerDocument) {
        apiClient.put("players/${player.uniqueId}/document") {
            contentType(ContentType.Application.Json)
            setBody(document)
        }
    }

    suspend fun sendPlayerJoinEventAsync(player: Player) {
        apiClient.post("/players/${player.uniqueId}/events/join") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun sendPlayerLeaveEventAsync(player: Player) {
        apiClient.post("/players/${player.uniqueId}/events/leave") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun serverStatusPostAsync(status: ServerStatus) {
        apiClient.post("/server/status") {
            contentType(ContentType.Application.Json)
            setBody(status)
        }
    }

    private suspend fun doApiHealthCheck() {
        val response = apiClient.get("/hc")

        if (!response.status.isSuccess()) {
            throw RuntimeException(
                "API health check failed with status ${response.status.value}. Ensure the API is running at $convexSiteUrl."
            )
        }

        plugin.logger.info("API health check successful.")
    }
}
