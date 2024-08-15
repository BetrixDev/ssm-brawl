package net.ssmb

import br.com.devsrsouza.kotlinbukkitapi.extensions.event
import br.com.devsrsouza.kotlinbukkitapi.extensions.events
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import net.ssmb.blockwork.Blockwork
import net.ssmb.blockwork.CollectionService
import net.ssmb.blockwork.hasTag
import net.ssmb.lifecycles.OnPluginDisable
import org.bukkit.GameMode
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.scheduler.BukkitTask

class SSMB : SuspendingJavaPlugin() {
    private lateinit var blockworkTickTask: BukkitTask
    private val pluginDisableListeners = arrayListOf<OnPluginDisable>()

    companion object {
        lateinit var instance: SSMB
    }

    override suspend fun onEnableAsync() {
        instance = this

        Blockwork.init {
            registerPlugin(this@SSMB)

            addPackage("net.ssmb.services")
            addPackage("net.ssmb.commands")
            addPackage("net.ssmb.components")

            container {
                register(this@SSMB)
                register(this@SSMB.logger)
            }
        }

        blockworkTickTask =
            server.scheduler.runTaskTimer(this, Runnable { Blockwork.doTick() }, 0L, 1L)

        Blockwork.modding.onListenerAdded<OnPluginDisable> { pluginDisableListeners.add(it) }

        Blockwork.modding.onListenerRemoved<OnPluginDisable> { pluginDisableListeners.remove(it) }

        events {
            event<EntityDamageEvent> {
                if (cause == EntityDamageEvent.DamageCause.FALL && !entity.hasTag("can_take_fall_damage")) {
                    isCancelled = true
                }
            }

            event<PlayerDropItemEvent> {
                isCancelled = true
            }

            event<PlayerInteractEvent> {
                player.resetCooldown()

                if (action == Action.RIGHT_CLICK_BLOCK) {
                    isCancelled = true
                }
            }

            event<BlockBreakEvent> {
                isCancelled = true
            }

            event<InventoryOpenEvent> {
                if (inventory.type != InventoryType.PLAYER && player.gameMode != GameMode.CREATIVE) {
                    isCancelled = true
                }
            }

            event<EntityDeathEvent> {
                CollectionService.removeTags(entity)
            }

            // We may want to store the tags of entities that unload so when the load back in again
            //  we can re apply the tags, similar to streaming in Roblox
            event<WorldUnloadEvent> {
                CollectionService.removeTags(world)

                world.entities.forEach { entity ->
                    CollectionService.removeTags(entity)
                }
            }
        }

        logger.info("STARTED SSMB")
    }

    override suspend fun onDisableAsync() {
        blockworkTickTask.cancel()

        pluginDisableListeners.forEach { it.onPluginDisable() }

        logger.info("STOPPED SSMB")
    }
}
