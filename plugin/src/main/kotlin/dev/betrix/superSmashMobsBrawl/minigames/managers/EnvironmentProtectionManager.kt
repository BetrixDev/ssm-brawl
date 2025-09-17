package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import gg.flyte.twilight.event.event
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.world.StructureGrowEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Manager responsible for protecting the minigame environment from player modifications. Prevents
 * block breaking, placing, and other world interactions that could disrupt gameplay.
 */
interface IEnvironmentProtectionManager : IManageable {}

class DefaultEnvironmentProtectionManager(private val minigame: BrawlMinigame) : Manageable(), IEnvironmentProtectionManager, KoinComponent {
    private val plugin: SuperSmashMobsBrawl by inject()

    override fun setup() {

        // Prevent block breaking
        listeners.add(
            event<BlockBreakEvent> {
                if (isPlayerInMinigame(player)) {
                    isCancelled = true
                }
            }
        )

        // Prevent block placing
        listeners.add(
            event<BlockPlaceEvent> {
                if (isPlayerInMinigame(player)) {
                    isCancelled = true
                }
            }
        )

        // Prevent normal damage event since we do it ourselves
        listeners.add(
            event<EntityDamageEvent> {
                plugin.logger.info("Cancelling EntityDamageEvent for ${entity.name} of type ${entity.type} with cause $cause due to minigame")
                if (entity is Player && isPlayerInMinigame(entity as Player)) {
                    isCancelled = true
                }
            }
        )

        listeners.add(
            event<PlayerPickupArrowEvent> {
                if (isPlayerInMinigame(player)) {
                    isCancelled = true
                }
            }
        )

        listeners.add(
            event<EntityPickupItemEvent> {
                if (entity is Player && isPlayerInMinigame(entity as Player)) {
                    isCancelled = true
                }
            }
        )

        // Prevent bucket usage (water/lava placement/collection)
        listeners.add(
            event<PlayerBucketEmptyEvent> {
                if (isPlayerInMinigame(player)) {
                    isCancelled = true
                }
            }
        )

        listeners.add(
            event<PlayerBucketFillEvent> {
                if (isPlayerInMinigame(player)) {
                    isCancelled = true
                }
            }
        )

        // Prevent interaction with blocks that have GUIs or functionality
        listeners.add(
            event<PlayerInteractEvent> {
                if (!isPlayerInMinigame(player)) return@event

                // Allow interaction with air (for abilities/combat)
                if (clickedBlock == null) return@event

                // Cancel interactions with containers, machines, etc.
                when (clickedBlock!!.type.name.uppercase()) {
                    "CHEST",
                    "TRAPPED_CHEST",
                    "ENDER_CHEST",
                    "FURNACE",
                    "BLAST_FURNACE",
                    "SMOKER",
                    "BREWING_STAND",
                    "ENCHANTING_TABLE",
                    "ANVIL",
                    "CHIPPED_ANVIL",
                    "DAMAGED_ANVIL",
                    "CRAFTING_TABLE",
                    "CARTOGRAPHY_TABLE",
                    "FLETCHING_TABLE",
                    "SMITHING_TABLE",
                    "STONECUTTER",
                    "GRINDSTONE",
                    "LOOM",
                    "BARREL",
                    "SHULKER_BOX",
                    "HOPPER",
                    "DISPENSER",
                    "DROPPER",
                    "OBSERVER",
                    "LECTERN",
                    "BEACON",
                    "JUKEBOX",
                    "NOTE_BLOCK",
                    "REDSTONE_WIRE",
                    "LEVER",
                    "STONE_BUTTON",
                    "OAK_BUTTON",
                    "BIRCH_BUTTON",
                    "SPRUCE_BUTTON",
                    "JUNGLE_BUTTON",
                    "ACACIA_BUTTON",
                    "DARK_OAK_BUTTON",
                    "CRIMSON_BUTTON",
                    "WARPED_BUTTON",
                    "POLISHED_BLACKSTONE_BUTTON",
                    "STONE_PRESSURE_PLATE",
                    "OAK_PRESSURE_PLATE",
                    "BIRCH_PRESSURE_PLATE",
                    "SPRUCE_PRESSURE_PLATE",
                    "JUNGLE_PRESSURE_PLATE",
                    "ACACIA_PRESSURE_PLATE",
                    "DARK_OAK_PRESSURE_PLATE",
                    "CRIMSON_PRESSURE_PLATE",
                    "WARPED_PRESSURE_PLATE",
                    "LIGHT_WEIGHTED_PRESSURE_PLATE",
                    "HEAVY_WEIGHTED_PRESSURE_PLATE",
                    "COMPARATOR",
                    "REPEATER",
                    "REDSTONE_TORCH",
                    "TRIPWIRE_HOOK",
                    "DAYLIGHT_DETECTOR" -> {
                        isCancelled = true
                    }
                }

                // Also cancel door/trapdoor interactions to prevent griefing
                if (
                    clickedBlock!!.type.name.contains("DOOR") ||
                        clickedBlock!!.type.name.contains("TRAPDOOR") ||
                        clickedBlock!!.type.name.contains("FENCE_GATE")
                ) {
                    isCancelled = true
                }
            }
        )

        // Prevent opening containers
        listeners.add(
            event<InventoryOpenEvent> {
                val player = this.player as? Player ?: return@event
                if (isPlayerInMinigame(player)) {
                    // Allow opening own inventory and equipment
                    if (inventory == player.inventory || inventory == player.enderChest) {
                        return@event
                    }

                    // Cancel opening world containers
                    isCancelled = true
                }
            }
        )

        // Prevent armor stand manipulation
        listeners.add(
            event<PlayerArmorStandManipulateEvent> {
                if (isPlayerInMinigame(player)) {
                    isCancelled = true
                }
            }
        )

        // Prevent breaking item frames, paintings, etc.
        listeners.add(
            event<HangingBreakByEntityEvent> {
                if (remover is Player && isPlayerInMinigame(remover as Player)) {
                    isCancelled = true
                }
            }
        )

        // Prevent entity damage to non-players (villagers, animals, etc.) unless it's
        // combat-related
        listeners.add(
            event<EntityDamageByEntityEvent> {
                if (damager is Player && isPlayerInMinigame(damager as Player)) {
                    // Allow player vs player damage (handled by combat manager)
                    if (entity is Player) return@event

                    // Prevent damage to other entities (animals, villagers, etc.)
                    isCancelled = true
                }
            }
        )

        // Prevent explosions from affecting the world (but allow damage)
        listeners.add(
            event<EntityExplodeEvent> {
                // If any minigame player is nearby, clear the block list to prevent terrain damage
                val minigameWorld = minigame.worldManager.getWorld()?.world
                if (entity.world == minigameWorld) {
                    blockList().clear()
                }
            }
        )

        // Prevent tree/crop growth during minigames to maintain map consistency
        listeners.add(
            event<StructureGrowEvent> {
                val minigameWorld = minigame.worldManager.getWorld()?.world
                if (world == minigameWorld) {
                    isCancelled = true
                }
            }
        )
    }

    private fun isPlayerInMinigame(player: Player): Boolean {
        return minigame.allPlayers().any {
            it.isOnline && it.player?.uniqueId == player.uniqueId
        } && !minigame.connectionManager.isDisconnected(player)
    }
}
