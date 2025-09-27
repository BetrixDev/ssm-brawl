package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.models.player.PlayerDocument
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
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
    private val apiSecretKey = System.getenv("API_SECRET_KEY")
    private val baseApiUrl = System.getenv("BASE_API_URL")

    @PublishedApi
    internal val apiClient =
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

    suspend inline fun <reified T> kvGetAsync(key: String): T? {
        val response = apiClient.get("kv/$key")

        if (response.status.value != 200) {
            return null
        }

        return response.body()
    }

    suspend fun kvSetAsync(key: String, value: Any) {
        apiClient.put("kv/$key") {
            contentType(ContentType.Application.Json)
            setBody(value)
        }
    }

    suspend fun kvDeleteAsync(key: String) {
        apiClient.delete("kv/$key")
    }
}
