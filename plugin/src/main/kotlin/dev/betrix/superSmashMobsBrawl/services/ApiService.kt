package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.models.player.PlayerDocument
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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player

object ApiService {
    private val httpAuthToken = System.getenv("HTTP_AUTH_TOKEN")
    private val httpBaseUrl = System.getenv("HTTP_BASE_URL")

    @PublishedApi
    internal val convexClient =
        HttpClient(CIO) {
            defaultRequest {
                if (!httpAuthToken.isNullOrBlank()) {
                    header("Authorization", "Bearer $httpAuthToken")
                }
                contentType(ContentType.Application.Json)
                url("$httpBaseUrl/convex")
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

    suspend fun playersGetDocumentAsync(player: Player, isJoinEvent: Boolean = false): PlayerDocument {
        val response: PlayerDocument = convexClient.get("players/${player.uniqueId}/document?joinEvent=${isJoinEvent}").body()

        return response
    }

    suspend fun playersSetDocumentAsync(player: Player, document: PlayerDocument) {
        convexClient.put("players/${player.uniqueId}/document") {
            contentType(ContentType.Application.Json)
            setBody(document)
        }
    }

    suspend inline fun <reified T> kvGetAsync(key: String): T? {
        val response: T = convexClient.get("/kv/$key").body()

        return response
    }

    suspend inline fun kvSetAsync(key: String, value: Any) {
        convexClient.put("/kv/$key") {
            contentType(ContentType.Application.Json)
            setBody(value)
        }
    }
}