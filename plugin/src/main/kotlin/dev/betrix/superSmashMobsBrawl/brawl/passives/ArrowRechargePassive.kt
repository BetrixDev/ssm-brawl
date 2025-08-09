package dev.betrix.superSmashMobsBrawl.brawl.passives

import dev.betrix.superSmashMobsBrawl.brawl.BrawlPassive
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.ItemStack

class ArrowRechargePassive(player: Player) : BrawlPassive("arrow_recharge", player) {

    private val arrowHotbarSlot = 2
    private val maximumArrowCount = 3
    private val arrowRechargeDelayTicks = 40L

    override fun setup() {
        val inv = player.inventory

        val initialArrows = ItemStack.of(Material.ARROW).apply { amount = maximumArrowCount }

        inv.setItem(arrowHotbarSlot, initialArrows)

        listeners.add(
            event<EntityShootBowEvent> {
                if (entity != player) {
                    return@event
                }

                synchronized(runnables) {
                    runnables.forEach { it.cancel() }
                    runnables.clear()

                    runnables.add(
                        repeatingTask(arrowRechargeDelayTicks, arrowRechargeDelayTicks) {
                            val arrowItemStack = inv.getItem(arrowHotbarSlot)
                            
                            if (arrowItemStack == null) {
                                val arrows = ItemStack.of(Material.ARROW).apply { amount = 1 }
                                inv.setItem(arrowHotbarSlot, arrows)
                                playPickupSound()
                                return@repeatingTask
                            }

                            if (arrowItemStack.type != Material.ARROW) {
                                return@repeatingTask
                            }

                            val newAmount =
                                (arrowItemStack.amount + 1).coerceAtMost(maximumArrowCount)

                            if (newAmount == arrowItemStack.amount) {
                                return@repeatingTask
                            }

                            arrowItemStack.amount = newAmount
                            inv.setItem(arrowHotbarSlot, arrowItemStack)
                            playPickupSound()
                        }
                    )
                }
            }
        )

        super.setup()
    }

    private fun playPickupSound() {
        player.playSound(player.eyeLocation, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
    }
}
