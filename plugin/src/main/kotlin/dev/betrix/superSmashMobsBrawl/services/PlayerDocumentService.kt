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
        runnables.add(
            repeatingTask(5.minutes.ticks) {
                documents.forEach { player, document ->
                    plugin.launch {
                        withContext(Dispatchers.IO) {
                            api.playersSetDocumentAsync(player, document)
                        }
                    }
                }
            }
        )

        listeners.add(
            event<PlayerJoinEvent>(priority = EventPriority.LOWEST) {
                runBlocking {
                    plugin.launch {
                        withContext(Dispatchers.IO) {
                            val document = api.playersGetDocumentAsync(player, true)
                            documents[player] = document
                        }
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

    fun getPlayerDocument(player: Player): PlayerDocument {
        val document = documents[player]

        if (document == null) {
            throw IllegalStateException("Player document for ${player.name} not found in cache.")
        } else {
            return document
        }
    }
}
