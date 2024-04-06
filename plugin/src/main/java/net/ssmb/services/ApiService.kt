package net.ssmb.services

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
import net.ssmb.dtos.minigame.*
import net.ssmb.dtos.player.BasicPlayerDataRequest
import net.ssmb.dtos.player.BasicPlayerDataResponse
import net.ssmb.dtos.player.IsIpBannedRequest
import net.ssmb.dtos.player.IsIpBannedResponse
import net.ssmb.dtos.queue.AddPlayerError
import net.ssmb.dtos.queue.AddPlayerRequest
import net.ssmb.dtos.queue.AddPlayerResponse
import net.ssmb.dtos.queue.RemovePlayerResponse
import org.bukkit.entity.Player

class ApiService {
    private val apiToken = System.getenv("API_AUTH_TOKEN")

    private val client = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            url("http://localhost:3000/api/")
            bearerAuth(apiToken)
            contentType(ContentType.Application.Json)
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun queueAddPlayer(player: Player, minigameId: String, force: Boolean?): AddPlayerResponse {
        val response = client.post("queue.addPlayer") {
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

    suspend fun queueRemovePlayers(playerUuids: List<String>): Int {
        val response = client.post("queue.removePlayer") {
            setBody(RemovePlayerResponse(playerUuids))
        }

        return response.status.value
    }

    suspend fun minigameStart(playerUuids: List<String>, minigameId: String): MinigameStartResponse {
        val response = client.post("minigame.start") {
            setBody(MinigameStartRequest(playerUuids, minigameId))
        }

        return when (response.status.value) {
            200 -> MinigameStartResponse.Success(json.decodeFromString(response.bodyAsText()))
            else -> MinigameStartResponse.Error(MiniGameError.UNKNOWN)
        }
    }

    suspend fun minigameEnd(payload: MinigameEndRequest): MinigameEndResponse {
        val response = client.post("minigame.end") {
            setBody(payload)
        }

        return when (response.status.value) {
            200 -> MinigameEndResponse.SUCCESS
            else -> MinigameEndResponse.ERROR
        }
    }

    suspend fun langAllEntries(): Map<String, String> {
        val response = client.get("lang.getAllEntries")

        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun playerBasicData(player: Player): BasicPlayerDataResponse {
        val response = client.post("player.getBasicPlayerData") {
            setBody(BasicPlayerDataRequest(player.uniqueId.toString()))
        }

        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun playerIsIpBanned(ip: String, player: Player): IsIpBannedResponse {
        val response = client.post("player.isIpBanned") {
            setBody(IsIpBannedRequest(ip, player.uniqueId.toString()))
        }

        return json.decodeFromString(response.bodyAsText())
    }
}