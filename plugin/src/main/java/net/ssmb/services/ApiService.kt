package net.ssmb.services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import net.ssmb.dtos.minigame.AddPlayerRequest
import net.ssmb.dtos.queue.AddPlayerError
import net.ssmb.dtos.queue.AddPlayerResponse
import net.ssmb.dtos.queue.minigame.MinigameStartRequest
import org.bukkit.entity.Player

class ApiService {
    private val apiToken = System.getenv("API_AUTH_TOKEN")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val client = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
        defaultRequest {
            url(System.getenv("API_ENDPOINT") ?: "http://localhost:3000")
            bearerAuth(apiToken)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun queueAddPlayer(player: Player, minigameId: String, force: Boolean?): AddPlayerResponse {
        val response = client.post("queue/add-player") {
            setBody(AddPlayerRequest(player.identity().uuid().toString(), minigameId, force))
        }

        val polymorphicJson = Json {
            ignoreUnknownKeys = true
        }

        return when (response.status.value) {
            200 -> polymorphicJson.decodeFromString(response.bodyAsText())
            409 -> AddPlayerResponse.Error(AddPlayerError.ALREADY_IN_QUEUE)
            else -> AddPlayerResponse.Error(AddPlayerError.UNKNOWN)
        }
    }

    suspend fun queueRemovePlayers(playerUuids: List<String>) {

    }

    suspend fun minigameStart(playerUuids: List<String>, minigameId: String) {
        val response = client.post("minigame/start") {
            setBody(MinigameStartRequest(playerUuids, minigameId))
        }
    }
}