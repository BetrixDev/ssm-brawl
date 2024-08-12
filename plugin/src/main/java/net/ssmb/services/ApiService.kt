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

@Service(loadOrder = -10)
class ApiService(private val plugin: SSMB) : OnStart {
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
}
