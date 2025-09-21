package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.extensions.event
import dev.betrix.superSmashMobsBrawl.extensions.ticks
import dev.betrix.superSmashMobsBrawl.models.player.PlayerDocument
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.minutes

object PlayerDocumentService : KoinComponent, Manageable() {
    private val api: ApiService by inject()

    private val documents = hashMapOf<Player, PlayerDocument>()

    override fun setup() {
        runnables.add(repeatingTask(5.minutes.ticks) {
            // auto-save task
        })
    }

    suspend fun fetchPlayerDocument(player: Player, isJoinEvent: Boolean = false) {
        withContext(Dispatchers.IO) {
            val document = api.playersGetDocumentAsync(player, isJoinEvent)
        }
    }

    suspend fun savePlayerDocument(player: Player) {}

    fun getPlayerDocument(player: Player): PlayerDocument {
        val document = documents[player]

        if (document == null) {
            throw IllegalStateException("Player document for ${player.name} not found in cache.")
        } else {
            return document
        }
    }
}