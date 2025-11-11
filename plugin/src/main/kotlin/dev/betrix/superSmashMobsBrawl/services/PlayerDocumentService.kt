package dev.betrix.superSmashMobsBrawl.services

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.events.PlayerDocumentLoaded
import gg.flyte.twilight.event.event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object PlayerDocumentService : KoinComponent, Manageable() {
    private val api: ApiService by inject()
    private val plugin: SuperSmashMobsBrawl by inject()

    override fun setup() {
        plugin.server.onlinePlayers.forEach { player ->
            plugin.launch {
                withContext(Dispatchers.IO) {
                    val task = async { api.playersEnsureDocumentAsync(player) }
                    async { api.sendPlayerJoinEventAsync(player) }

                    task.await()

                    withContext(plugin.minecraftDispatcher) {
                        PlayerDocumentLoaded(player).callEvent()
                    }
                }
            }
        }

        listeners.add(
                event<PlayerJoinEvent>(priority = EventPriority.LOWEST) {
                    runBlocking {
                        plugin.launch {
                            withContext(Dispatchers.IO) {
                                val task = async { api.playersEnsureDocumentAsync(player) }
                                async { api.sendPlayerJoinEventAsync(player) }

                                task.await()

                                withContext(plugin.minecraftDispatcher) {
                                    PlayerDocumentLoaded(player).callEvent()
                                }
                            }
                        }
                    }
                }
        )

        listeners.add(
                event<PlayerQuitEvent>(priority = EventPriority.LOWEST) {
                    plugin.launch {
                        withContext(Dispatchers.IO) {
                            async { api.sendPlayerLeaveEventAsync(player) }
                        }
                    }
                }
        )
    }
}
