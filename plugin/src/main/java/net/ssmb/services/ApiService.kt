package net.ssmb.services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import net.ssmb.SSMB
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.interfaces.OnStart
import org.bukkit.entity.Player

@Service(loadOrder = -10)
class ApiService(private val plugin: SSMB) : OnStart {
    private val env = System.getenv()
    private val secretToken = env["API_TOKEN_SECRET"] ?: ""
    private val apiHost = env["API_HOST"] ?: "localhost"
    private val apiPort = env["API_PORT"] ?: "8080"
    private val apiProtocol = env["API_PROTOCOL"] ?: "http"

    private val client =
        HttpClient(CIO) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(ContentNegotiation) { json() }
            install(HttpCookies) { storage = AcceptAllCookiesStorage() }
            defaultRequest {
                header("User-Agent", "ssmb_plugin")
                url("$apiProtocol://$apiHost:$apiPort")
                contentType(ContentType.Application.Json)
            }
        }

    private val json = Json { ignoreUnknownKeys = true }

    override fun onStart() {
        runBlocking {
            val tokenGeneratorResponse =
                client.post("generateToken/plugin") { headers { append("Secret", secretToken) } }

            if (tokenGeneratorResponse.status.value != 200) {
                plugin.logger.severe(
                    "Failed to generate token. Status code: ${tokenGeneratorResponse.status.value}"
                )
                throw Exception("Failed to generate token.")
            }
        }
    }

    suspend fun fetchPlayerData(player: Player): PlayerDataRecord {
        return PlayerDataRecord("creeper")
    }

    suspend fun savePlayerData(player: Player, data: PlayerDataRecord) {
        // stuff
    }
}
