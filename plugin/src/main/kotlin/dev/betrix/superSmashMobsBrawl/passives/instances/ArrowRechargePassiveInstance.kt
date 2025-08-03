package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.ItemStack

class ArrowRechargePassiveInstance(definition: PassiveDefinition, player: Player) : PassiveInstance(definition, player) {
    private val arrowHotbarSlot = 2
    private val arrowRechargeDelayTicks = 40L
    private val maximumArrowCount = 3

    override fun setup() {
        listeners.add(event<EntityShootBowEvent> {
            if (entity != player) {
                return@event
            }

            runnables.forEach {
                it.cancel()
            }
            runnables.clear()

            runnables.add(repeatingTask(arrowRechargeDelayTicks, arrowRechargeDelayTicks) {
                val arrowItemStack = player.inventory.getItem(arrowHotbarSlot)

                if (arrowItemStack == null) {
                    val arrows = ItemStack.of(Material.ARROW).apply {
                        amount = 1
                    }

                    player.inventory.setItem(arrowHotbarSlot, arrows)
                    playPickupSound()

                    return@repeatingTask
                }

                if (arrowItemStack.amount >= maximumArrowCount) {
                    return@repeatingTask
                }

                arrowItemStack.amount += 1
                playPickupSound()
            })
        })
    }

    private fun playPickupSound() {
        player.playSound(player.eyeLocation, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
    }
}