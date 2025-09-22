package dev.betrix.superSmashMobsBrawl.services

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.extensions.ticks
import dev.betrix.superSmashMobsBrawl.extensions.warn
import dev.betrix.superSmashMobsBrawl.models.player.PlayerDocument
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object PlayerDocumentService : KoinComponent, Manageable() {
    private val api: ApiService by inject()
    private val plugin: SuperSmashMobsBrawl by inject()

    private val documents = hashMapOf<UUID, PlayerDocument>()

    override fun setup() {
        plugin.server.onlinePlayers.forEach { player ->
            plugin.launch {
                val document =
                    withContext(Dispatchers.IO) { api.playersGetDocumentAsync(player, true) }

                documents[player.uniqueId] = document
            }
        }

        runnables.add(
            repeatingTask(5.minutes.ticks) {
                val entries = documents.entries.toList()
                entries.forEach { (uuid, document) ->
                    plugin.launch {
                        withContext(Dispatchers.IO) {
                            val player = plugin.server.getPlayer(uuid)

                            if (player == null) {
                                plugin.logger.warn(
                                    "Player with UUID {uuid} not found, removing from cache and skipping persistence.",
                                    uuid,
                                )
                                documents.remove(uuid)
                                return@withContext
                            }

                            runCatching { api.playersSetDocumentAsync(player, document) }
                                .onFailure {
                                    plugin.logger.warn(
                                        "Failed to persist document for {playerName}: {errorMessage}",
                                        player.name,
                                        it.message ?: "No error message",
                                    )
                                }
                        }
                    }
                }
            }
        )

        listeners.add(
            event<PlayerJoinEvent>(priority = EventPriority.LOWEST) {
                runBlocking {
                    plugin.launch {
                        val document =
                            withContext(Dispatchers.IO) {
                                api.playersGetDocumentAsync(player, true)
                            }

                        documents[player.uniqueId] = document
                    }
                }
            }
        )

        listeners.add(
            event<PlayerQuitEvent>(priority = EventPriority.LOWEST) {
                documents.remove(player.uniqueId)?.let {
                    plugin.launch {
                        withContext(Dispatchers.IO) { api.playersSetDocumentAsync(player, it) }
                    }
                }
            }
        )
    }

    override fun teardown() {
        val entries = documents.entries.toList()
        runBlocking {
            entries.forEach { (uuid, doc) ->
                val player = plugin.server.getPlayer(uuid)

                if (player == null) {
                    plugin.logger.warn(
                        "Player with UUID {uuid} not found, skipping persistence.",
                        uuid,
                    )
                    return@forEach
                }

                withContext(Dispatchers.IO) {
                    runCatching { api.playersSetDocumentAsync(player, doc) }
                        .onFailure {
                            plugin.logger.warn(
                                "Flush failed for {playerName}: {errorMessage}",
                                player.name,
                                it.message ?: "No error message",
                            )
                        }
                }
            }
        }
        documents.clear()
    }

    fun getPlayerDocument(player: Player): PlayerDocument =
        documents[player.uniqueId]
            ?: error("Player document for ${player.name} not found in cache.")

    fun getPlayerDocumentOrNull(player: Player): PlayerDocument? = documents[player.uniqueId]

    suspend fun getPlayerDocumentOrFetch(player: Player): PlayerDocument {
        return documents[player.uniqueId]
            ?: withContext(Dispatchers.IO) {
                val document = api.playersGetDocumentAsync(player, true)
                documents[player.uniqueId] = document
                document
            }
    }
}
