package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.models.player.PlayerDocument
import dev.betrix.superSmashMobsBrawl.utils.DockerDetector
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player

object ApiService : IManageable {
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
            "http://api:3000/plugin"
        } else {
            "http://localhost:3000/plugin"
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
            install(HttpRequestRetry) {
                retryOnServerErrors(2)
            }
        }

    override fun setup() {
        runBlocking {
            val response = apiClient.get("/hc")

            if (response.status.value != 200) {
                if (isDocker) {
                    throw RuntimeException(
                        "API health check failed with status ${response.status.value}. " +
                            "Ensure the API container is running and accessible.",
                    )
                }

                throw RuntimeException("API health check failed with status ${response.status.value}. " +
                    "If you're running the server locally, ensure the API is running at $baseApiUrl.")
            }
        }
    }

    override fun teardown() {
        apiClient.close()
    }

    suspend fun playersGetDocumentAsync(
        player: Player,
    ): PlayerDocument {
        val response: PlayerDocument =
            apiClient.get("players/${player.uniqueId}/document").body()

        return response
    }

    suspend fun sendPlayerJoinEvent(
        player: Player,
    ) {
        apiClient.post("players/${player.uniqueId}/join")
    }

    suspend fun sendPlayerQuitEvent(
        player: Player,
    ) {
        apiClient.post("players/${player.uniqueId}/quit")
    }

    suspend fun playersSetDocumentAsync(player: Player, document: PlayerDocument) {
        apiClient.put("players/${player.uniqueId}/document") {
            contentType(ContentType.Application.Json)
            setBody(document)
        }
    }
}
