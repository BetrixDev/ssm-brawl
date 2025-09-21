package dev.betrix.superSmashMobsBrawl.services

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.extensions.ticks
import dev.betrix.superSmashMobsBrawl.models.player.PlayerDocument
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
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

    private val documents = hashMapOf<Player, PlayerDocument>()

    override fun setup() {
        plugin.server.onlinePlayers.forEach { player ->
            plugin.launch {
                val document =
                    withContext(Dispatchers.IO) { api.playersGetDocumentAsync(player, true) }

                documents[player] = document
            }
        }

        runnables.add(
            repeatingTask(5.minutes.ticks) {
                val entries = documents.entries.toList()
                entries.forEach { (player, document) ->
                    plugin.launch {
                        withContext(Dispatchers.IO) {
                            runCatching { api.playersSetDocumentAsync(player, document) }
                                .onFailure {
                                    plugin.logger.warning(
                                        "Failed to persist document for ${player.name}: ${it.message}"
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

                        documents[player] = document
                    }
                }
            }
        )

        listeners.add(
            event<PlayerQuitEvent>(priority = EventPriority.LOWEST) {
                documents.remove(player)?.let {
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
            entries.forEach { (player, doc) ->
                withContext(Dispatchers.IO) {
                    runCatching { api.playersSetDocumentAsync(player, doc) }
                        .onFailure {
                            plugin.logger.warning("Flush failed for ${player.name}: ${it.message}")
                        }
                }
            }
        }
        documents.clear()
    }

    fun getPlayerDocument(player: Player): PlayerDocument =
        documents[player] ?: error("Player document for ${player.name} not found in cache.")
}
