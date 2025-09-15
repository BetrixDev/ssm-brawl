package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.events.PlayerSelectKitEvent
import dev.betrix.superSmashMobsBrawl.extensions.event
import dev.betrix.superSmashMobsBrawl.kits.BrawlKit
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitSwitchingMode
import gg.flyte.twilight.event.event
import gg.flyte.twilight.gui.GUI.Companion.openInventory
import gg.flyte.twilight.gui.gui
import io.papermc.paper.datacomponent.DataComponentTypes
import java.util.concurrent.ConcurrentHashMap
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class AssignKitError {
    PLAYER_HAS_KIT
}

object KitService : KoinComponent {
    private val lang: LangService by inject()
    private val plugin: JavaPlugin by inject()
    private val dataService: DataService by inject()
    private val minigameService: MinigameService by inject()

    private val playerSelectedKits = ConcurrentHashMap<OfflinePlayer, String>() // kit id
    private val assignedBrawlKits = ConcurrentHashMap<OfflinePlayer, BrawlKit>()

    init {
        event<PlayerQuitEvent> {
            unassignKit(player)
            playerSelectedKits.remove(player)
        }
    }

    fun playerSelectKit(player: Player, kit: KitDef) {
        playerSelectedKits[player] = kit.id

        // Determine if the player should switch kits immediately based on their current minigame
        val currentMinigame = minigameService.getMinigameForPlayer(player)
        val shouldSwitchImmediately =
            currentMinigame?.getKitSwitchingMode() == KitSwitchingMode.IMMEDIATE

        // Dispatch the event
        PlayerSelectKitEvent.call(player, kit, shouldSwitchImmediately)
    }

    fun currentSelectedKitForPlayer(player: OfflinePlayer): KitDef {
        val kitId = playerSelectedKits[player]

        return kitId?.let { dataService.getKit(kitId) } ?: dataService.getKit(defaultKitId())!!
    }

    fun assignKit(player: OfflinePlayer): Result<BrawlKit, AssignKitError> {
        if (!playerSelectedKits.containsKey(player) || playerSelectedKits[player] == null) {
            playerSelectedKits[player] = defaultKitId()
        }
        return assignKit(player, playerSelectedKits[player] ?: defaultKitId())
    }

    fun assignKit(player: OfflinePlayer, kitId: String): Result<BrawlKit, AssignKitError> {
        if (assignedBrawlKits.containsKey(player)) {
            unassignKit(player)
        }

        if (!player.isOnline) {
            return Err(AssignKitError.PLAYER_HAS_KIT) // Player must be online to assign kit
        }

        val kitData = dataService.getKit(kitId) ?: dataService.getKit(defaultKitId())!!

        val brawlKit =
            when (kitData.id) {
                else -> BrawlKit(kitData.id, player.player!!)
            }

        assignedBrawlKits[player] = brawlKit
        brawlKit.setup()
        return Ok(brawlKit)
    }

    fun assignKit(player: OfflinePlayer, kit: KitDef) {
        if (assignedBrawlKits.containsKey(player)) {
            unassignKit(player)
        }

        if (player.isOnline) {
            assignedBrawlKits[player] = BrawlKit(kit.id, player.player!!).apply { setup() }
        }
    }

    fun unassignKit(player: OfflinePlayer): BrawlKit? {
        val kit = assignedBrawlKits.remove(player)
        kit?.let { instance ->
            plugin.launch {
                try {
                    instance.teardown()
                } catch (e: Exception) {
                    plugin.logger.warning(
                        "Error during kit teardown for player ${player.name}: ${e.message}"
                    )
                }
            }
        }
        return kit
    }

    fun getKitForPlayer(player: Player): BrawlKit? = assignedBrawlKits[player]

    fun hasKit(player: Player): Boolean = assignedBrawlKits.containsKey(player)

    fun getKitData(id: String): KitDef? {
        return dataService.getKit(id)
    }

    fun getAllKitData(): List<KitDef> {
        return dataService.getAllKits()
    }

    fun findClosestKitById(id: String): KitDef? {
        if (id.isBlank()) return null

        getKitData(id)?.let {
            return it
        }

        getAllKitData()
            .filter { it.userFacing }
            .find { it.id.equals(id, ignoreCase = true) }
            ?.let {
                return it
            }

        return getAllKitData()
            .filter { it.userFacing }
            .find { it.id.contains(id, ignoreCase = true) }
    }

    fun openKitSelectionGui(player: Player) {
        val currentSelectedKit = currentSelectedKitForPlayer(player)

        val guiColumns = 9
        val guiRows = 4
        val filledSlots = hashSetOf<Int>()

        val kitSelectionGui =
            gui(lang.t("gui.kitSelection.title"), guiColumns * guiRows) {
                onClick { isCancelled = true }

                getAllKitData()
                    .filter { it.userFacing && it.displayItem != null }
                    .forEachIndexed { idx, kit ->
                        set(
                            getCenteredSlot(guiColumns, guiRows, idx).apply {
                                filledSlots.add(this)
                            },
                            ItemStack.of(kit.displayItem!!).apply {
                                val isKitSelected = currentSelectedKit.id == kit.id

                                val meta = itemMeta

                                if (isKitSelected) {
                                    meta.displayName(
                                        lang.t("gui.kitSelection.selectedKitName") {
                                            "kitId" to kit.id
                                        }
                                    )
                                } else {
                                    meta.displayName(
                                        lang.t("gui.kitSelection.kitName") { "kitId" to kit.id }
                                    )
                                }

                                val loreList = arrayListOf<Component>()

                                loreList.add(lang.t("gui.kitSelection.abilityList.title"))

                                val abilityList =
                                    kit.abilities.map {
                                        lang.t("gui.kitSelection.abilityList.entry") {
                                            "abilityId" to it.id
                                        }
                                    }
                                loreList.addAll(abilityList)

                                meta.lore(loreList)

                                itemMeta = meta

                                setData(
                                    DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
                                    isKitSelected,
                                )
                            },
                        ) {
                            playerSelectKit(player, kit)
                            player.closeInventory()
                            player.sendMessage(
                                lang.t("messages.kits.select.success") { "kitId" to kit.id }
                            )
                            player.playSound(
                                player.location,
                                kit.selectionSound ?: Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                1f,
                                1f,
                            )
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

        player.openInventory(kitSelectionGui)
    }

    private fun defaultKitId(): String {
        return dataService.getKit("creeper")?.id ?: dataService.getKit("skeleton")?.id ?: "creeper"
    }
}

fun getCenteredSlot(columns: Int, rows: Int, index: Int): Int {
    if (index <= 0) {
        val centerRow = rows / 2
        val centerCol = columns / 2
        return centerRow * columns + centerCol
    }

    val centerRow = rows / 2
    val centerCol = columns / 2

    // Prevent infinite loops when there are more items than slots
    if (index >= columns * rows) {
        return centerRow * columns + centerCol
    }

    // Calculate which "layer" this index belongs to
    var currentIndex = 1
    var layer = 1

    while (currentIndex <= index) {
        val itemsInLayer = getItemsInLayer(layer, centerRow, centerCol, rows, columns)
        if (itemsInLayer == 0) {
            break
        }

        if (currentIndex + itemsInLayer > index) {
            // This index is in the current layer
            val positionInLayer = index - currentIndex
            return getSlotInLayer(layer, positionInLayer, centerRow, centerCol, rows, columns)
        }
        currentIndex += itemsInLayer
        layer++
    }

    // Fallback to center slot
    return centerRow * columns + centerCol
}

private fun getItemsInLayer(
    layer: Int,
    centerRow: Int,
    centerCol: Int,
    rows: Int,
    columns: Int,
): Int {
    var count = 0

    for (r in 0 until rows) {
        for (c in 0 until columns) {
            val manhattanDistance = kotlin.math.abs(r - centerRow) + kotlin.math.abs(c - centerCol)
            if (manhattanDistance == layer) {
                count++
            }
        }
    }

    return count
}

private fun getSlotInLayer(
    layer: Int,
    positionInLayer: Int,
    centerRow: Int,
    centerCol: Int,
    rows: Int,
    columns: Int,
): Int {
    var currentPos = 0

    for (r in 0 until rows) {
        for (c in 0 until columns) {
            val manhattanDistance = kotlin.math.abs(r - centerRow) + kotlin.math.abs(c - centerCol)
            if (manhattanDistance == layer) {
                if (currentPos == positionInLayer) {
                    return r * columns + c
                }
                currentPos++
            }
        }
    }

    // Fallback to center
    return centerRow * columns + centerCol
}
