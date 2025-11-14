package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.events.QueuePopEvent
import dev.betrix.superSmashMobsBrawl.extensions.playErrorSound
import dev.betrix.superSmashMobsBrawl.gui.BrawlGui.Companion.openInventory
import dev.betrix.superSmashMobsBrawl.gui.brawlGui
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import dev.betrix.superSmashMobsBrawl.services.getCenteredSlot
import gg.flyte.twilight.scheduler.repeatingTask
import io.papermc.paper.datacomponent.DataComponentTypes
import java.util.logging.Logger
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// This will be easy to add party data to in the future if we want
data class QueueEntry(val player: Player, val minigame: MinigameDef, val partyId: String? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (javaClass != other?.javaClass) {
            return false
        }

        other as QueueEntry

        return player == other.player
    }

    override fun hashCode(): Int {
        return player.hashCode()
    }
}

object QueueService : Manageable(), KoinComponent {
    private val logger: Logger by inject()
    private val minigameService: MinigameService by inject()
    private val dataService: DataService by inject()
    private val lang: LangService by inject()

    private val queue = hashSetOf<QueueEntry>()

    init {
        runnables.add(
            repeatingTask(20) {
                // Periodically check if any queued minigame can start
                checkAllMinigamesCanStart()
            }
        )
    }

    fun addPlayer(player: Player, minigameDefinition: MinigameDef): Result<QueueEntry, QueueEntry> {
        val newEntry = QueueEntry(player, minigameDefinition)

        val existingEntry = queue.find { it.player == player }

        if (existingEntry != null) {
            return Err(existingEntry)
        }

        queue.add(newEntry)

        return Ok(newEntry)
    }

    fun removePlayer(player: Player): Result<QueueEntry, Unit> {
        val existingEntry = queue.find { it.player == player }

        return if (existingEntry != null) {
            queue.remove(existingEntry)
            Ok(existingEntry)
        } else {
            Err(Unit)
        }
    }

    fun getQueueEntry(player: Player): QueueEntry? {
        return queue.find { it.player == player }
    }

    fun getPlayersInQueue(minigameDef: MinigameDef): List<QueueEntry> {
        return queue.filter { it.minigame.id == minigameDef.id }
    }

    private fun getRequiredPlayersForMinigame(minigameDef: MinigameDef): Int {
        return when (minigameDef) {
            is TeamBasedStocksMinigameDef -> {
                minigameDef.playersPerTeam * minigameDef.amountOfTeams
            }

            is FfaMinigameDef -> {
                // Use the minimum to allow starting when the game defines it can
                minigameDef.minPlayers
            }
        }
    }

    private fun checkAllMinigamesCanStart() {
        // Snapshot the queue to determine which minigame types are present
        val snapshot = queue.toList()

        // Map unique minigame id -> definition
        val defsById = snapshot.groupBy { it.minigame.id }.mapValues { it.value.first().minigame }

        defsById.values.forEach { def -> tryStartMinigamesFor(def) }
    }

    private fun tryStartMinigamesFor(minigameDef: MinigameDef) {
        val requiredPlayers = getRequiredPlayersForMinigame(minigameDef)

        if (requiredPlayers <= 0) {
            logger.severe(
                "Minigame ${minigameDef.id} has invalid player requirement: $requiredPlayers"
            )
            return
        }

        // Filter out any players who may have entered a minigame meanwhile
        var available =
            getPlayersInQueue(minigameDef)
                .filter { !minigameService.isPlayerInMinigame(it.player) }
                .toMutableList()

        while (available.size >= requiredPlayers) {
            val playersToStart = available.take(requiredPlayers)

            // Remove chosen entries from the master queue
            playersToStart.forEach { queue.remove(it) }

            QueuePopEvent(minigameDef.id, playersToStart.map { it.player }).callEvent()

            // Drop the used players from the local list and continue if we can start more
            available = available.drop(requiredPlayers).toMutableList()
        }
    }

    fun openQueueSelectionGui(player: Player) {
        val currentQueueEntry = getQueueEntry(player)

        val guiColumns = 9
        val guiRows = 4
        val filledSlots = hashSetOf<Int>()

        val queueSelectionGui =
            brawlGui(lang.t("gui.queueSelection.title"), guiColumns * guiRows) {
                onClick { isCancelled = true }

                dataService
                    .getAllMinigames()
                    .filter { !it.isHidden && it.displayItem != null }
                    .forEachIndexed { idx, minigame ->
                        set(
                            getCenteredSlot(guiColumns, guiRows, idx).apply {
                                filledSlots.add(this)
                            },
                            ItemStack.of(minigame.displayItem!!).apply {
                                val isInQueue =
                                    currentQueueEntry?.minigame?.id == minigame.id

                                val meta = itemMeta

                                if (isInQueue) {
                                    meta.displayName(
                                        lang.t("gui.queueSelection.queuedMinigameName") {
                                            "minigameId" to minigame.id
                                        }
                                    )
                                } else {
                                    meta.displayName(
                                        lang.t("gui.queueSelection.minigameName") {
                                            "minigameId" to minigame.id
                                        }
                                    )
                                }

                                val loreList = arrayListOf<Component>()

                                loreList.add(
                                    lang.t("gui.queueSelection.description") {
                                        "minigameId" to minigame.id
                                    }
                                )

                                val playersInQueue = getPlayersInQueue(minigame).size
                                val requiredPlayers = getRequiredPlayersForMinigame(minigame)

                                loreList.add(
                                    lang.t("gui.queueSelection.playerCount") {
                                        "current" to playersInQueue.toString()
                                    }
                                )

                                loreList.add(
                                    lang.t("gui.queueSelection.requiredPlayers") {
                                        "required" to requiredPlayers.toString()
                                    }
                                )

                                loreList.add(Component.empty())

                                if (isInQueue) {
                                    loreList.add(lang.t("gui.queueSelection.clickToLeave"))
                                } else {
                                    loreList.add(lang.t("gui.queueSelection.clickToJoin"))
                                }

                                meta.lore(loreList)

                                itemMeta = meta

                                setData(
                                    DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
                                    isInQueue,
                                )
                            },
                        ) {
                            val currentQueueEntry = getQueueEntry(player)

                            if (currentQueueEntry != null) {
                                if (currentQueueEntry.minigame.id == minigame.id) {
                                    // Leave queue
                                    removePlayer(player)
                                    player.closeInventory()
                                    player.sendMessage(
                                        lang.t("messages.queue.leave.success") {
                                            "minigameId" to minigame.id
                                        }
                                    )
                                    player.playSound(
                                        player.location,
                                        Sound.BLOCK_NOTE_BLOCK_BASS,
                                        1f,
                                        0.5f,
                                    )
                                } else {
                                    // Already in a different queue
                                    player.sendMessage(
                                        lang.t("messages.queue.join.alreadyInQueue")
                                    )
                                    player.playErrorSound()
                                }
                            } else {
                                // Join queue
                                addPlayer(player, minigame)
                                player.closeInventory()
                                player.sendMessage(
                                    lang.t("messages.queue.join.success") {
                                        "minigameId" to minigame.id
                                    }
                                )
                                player.playSound(
                                    player.location,
                                    Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                    1f,
                                    1f,
                                )
                            }
                        }
                    }

                for (cellIdx in 0 until guiColumns * guiRows) {
                    if (filledSlots.contains(cellIdx)) {
                        continue
                    }

                    set(
                        cellIdx,
                        ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
                            val meta = itemMeta

                            meta.displayName(Component.empty())

                            itemMeta = meta
                        },
                    )
                }
            }

        player.openInventory(queueSelectionGui)
    }
}
