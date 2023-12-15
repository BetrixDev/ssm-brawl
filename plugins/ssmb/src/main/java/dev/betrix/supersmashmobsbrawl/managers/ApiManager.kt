package dev.betrix.supersmashmobsbrawl.managers

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player

class ApiManager {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.DEFAULT
        }
        defaultRequest {
            url(System.getenv("API_ENDPOINT") ?: "http://localhost:3000")
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val apiToken = System.getenv("API_AUTH_TOKEN")

    init {
        if (apiToken == null) {
            throw IllegalArgumentException("API_AUTH_TOKEN is not set")
        }
    }

    suspend fun fetchPlayerData(player: Player): PlayerDataResponse {
        SuperSmashMobsBrawl.instance.logger.info(player.uniqueId.toString())
        val response = client.post("api/player/general") {
            headers {
                append(
                    "Authorization",
                    "Bearer $apiToken"
                )
            }
            contentType(ContentType.Application.Json)
            setBody(PlayerDataRequest(player.uniqueId.toString()))
        }

        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun addPlayerToQueue(player: Player, modeId: String, isRanked: Boolean): JoinQueue {
        val response = client.post("api/queue/join") {
            headers {
                append(
                    "Authorization",
                    "Bearer $apiToken"
                )
            }
            contentType(ContentType.Application.Json)
            setBody(JoinQueueRequest(player.identity().uuid().toString(), modeId, isRanked))
        }

        // Using a custom json builder since the JoinQueueResponse is polymorphic
        val polymorphicJson = Json {
            classDiscriminator = "action"
        }

        return when (response.status.value) {
            200 -> JoinQueue.Success(polymorphicJson.decodeFromString(response.bodyAsText()))
            409 -> JoinQueue.Error(JoinQueueError.ALREADY_IN_QUEUE)
            else -> JoinQueue.Error(JoinQueueError.UNKNOWN)
        }
    }

    suspend fun removePlayerFromQueue(player: Player): LeaveQueue {
        val response = client.post("api/queue/leave") {
            headers {
                append(
                    "Authorization",
                    "Bearer $apiToken"
                )
            }
            contentType(ContentType.Application.Json)
            setBody(LeaveQueueRequest(player.identity().uuid().toString()))
        }

        return when (response.status.value) {
            200 -> LeaveQueue.Success(json.decodeFromString((response.bodyAsText())))
            409 -> LeaveQueue.Error(LeaveQueueError.NOT_IN_QUEUE)
            else -> LeaveQueue.Error(LeaveQueueError.UNKNOWN)
        }
    }

    // Only use this when shutting down the server to clear all queues of players
    suspend fun clearQueue() {
        client.delete("api/queue/clear") {
            headers {
                append(
                    "Authorization",
                    "Bearer $apiToken"
                )
            }
        }
    }

    suspend fun tryStartGame(players: List<Player>, modeId: String, isRanked: Boolean): StartGame {
        val playerUuids = players.map { it.identity().uuid().toString() }

        val response = client.post("api/minigames/start") {
            headers {
                append(
                    "Authorization",
                    "Bearer $apiToken"
                )
            }
            contentType(ContentType.Application.Json)
            setBody(StartGameRequest(playerUuids, modeId, isRanked))
        }
        
        return when (response.status.value) {
            200 -> StartGame.Success(json.decodeFromString((response.bodyAsText())))
            else -> StartGame.Error(StartGameError.UNKNOWN)
        }
    }

    fun destroyClient() {
        client.close()
    }
}