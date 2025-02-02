package net.ssmb.services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import net.ssmb.SSMB
import net.ssmb.dtos.maps.GetMapDetailsRequest
import net.ssmb.dtos.maps.GetMapDetailsResponse
import net.ssmb.dtos.minigame.*
import net.ssmb.dtos.player.*
import net.ssmb.dtos.queue.AddPlayerError
import net.ssmb.dtos.queue.AddPlayerRequest
import net.ssmb.dtos.queue.AddPlayerResponse
import net.ssmb.dtos.queue.RemovePlayerResponse
import org.bukkit.entity.Player

class ApiService {
    private val secretToken = System.getenv("API_TOKEN_SECRET")
    private val apiHost = System.getenv("API_HOST")
    private val apiPort = System.getenv("API_PORT")
    private val apiProtocol = System.getenv("API_PROTOCOL")

    private val client =
        HttpClient(CIO) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(ContentNegotiation) { json() }
            install(HttpCookies) { storage = AcceptAllCookiesStorage() }
            defaultRequest {
                url("$apiProtocol://$apiHost:$apiPort")
                contentType(ContentType.Application.Json)
            }
        }

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun initialize() {
        val tokenGeneratorResponse =
            client.post("generateToken/plugin") { headers { append("Secret", secretToken) } }

        SSMB.instance.logger.info(tokenGeneratorResponse.setCookie().toString())

        if (tokenGeneratorResponse.status.value != 200) {
            throw RuntimeException("Error grabbing token for api")
        }
    }

    suspend fun queueAddPlayer(
        player: Player,
        minigameId: String,
        force: Boolean?
    ): AddPlayerResponse {
        val response =
            client.post("api/queue.addPlayer") {
                setBody(AddPlayerRequest(player.identity().uuid().toString(), minigameId, force))
            }

        val polymorphicJson = Json {
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }

        return when (response.status.value) {
            200 ->
                AddPlayerResponse.Success(polymorphicJson.decodeFromString(response.bodyAsText()))
            409 -> AddPlayerResponse.Error(AddPlayerError.ALREADY_IN_QUEUE)
            else -> AddPlayerResponse.Error(AddPlayerError.UNKNOWN)
        }
    }

    suspend fun queueRemovePlayers(playerUuids: List<String>): Int {
        val response =
            client.post("api/queue.removePlayers") { setBody(RemovePlayerResponse(playerUuids)) }

        return response.status.value
    }

    suspend fun queueFlush(): Int {
        val response = client.post("api/queue.flushQueue")

        return response.status.value
    }

    suspend fun minigameStart(
        teams: List<MinigameStartRequest.TeamEntry>,
        minigameId: String
    ): MinigameStartResponse {
        val response =
            client.post("api/minigame.start") { setBody(MinigameStartRequest(teams, minigameId)) }

        return when (response.status.value) {
            200 -> MinigameStartResponse.Success(json.decodeFromString(response.bodyAsText()))
            else -> MinigameStartResponse.Error(MiniGameError.UNKNOWN)
        }
    }

    suspend fun minigameEnd(payload: MinigameEndRequest): MinigameEndResponse {
        val response = client.post("api/minigame.end") { setBody(payload) }

        return when (response.status.value) {
            200 -> MinigameEndResponse.SUCCESS
            else -> MinigameEndResponse.ERROR
        }
    }

    suspend fun minigameGetPlayableGames(): List<PlayableGamesResponseEntry> {
        val response = client.get("api/minigame.getPlayableGames")

        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun langGetAllEntries(): Map<String, String> {
        val response = client.get("api/lang.getAllEntries")

        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun playerBasicData(player: Player): BasicPlayerDataResponse {
        val response =
            client.post("api/player.getBasicPlayerData") {
                setBody(BasicPlayerDataRequest(player.uniqueId.toString()))
            }

        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun playerIsIpBanned(ip: String, player: Player): IsIpBannedResponse {
        val response =
            client.post("api/player.isIpBanned") {
                setBody(IsIpBannedRequest(ip, player.uniqueId.toString()))
            }

        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun playerUpdatePlayerName(player: Player): Int {
        val request = UpdatePlayerNameRequest(player.uniqueId.toString(), player.name)

        val response = client.post("api/player.updatePlayerName") { setBody(request) }

        return response.status.value
    }

    suspend fun serverBeginShutdown(): Int {
        val response = client.post("api/server.beginShutdown")

        return response.status.value
    }

    suspend fun mapGetMapDetails(mapId: String): GetMapDetailsResponse {
        val response =
            client.post("api/maps.getMapDetails") { setBody(GetMapDetailsRequest(mapId)) }

        if (response.status.value == 403) {
            throw RuntimeException("Unable to find map with id $mapId")
        }

        return json.decodeFromString(response.bodyAsText())
    }
}
