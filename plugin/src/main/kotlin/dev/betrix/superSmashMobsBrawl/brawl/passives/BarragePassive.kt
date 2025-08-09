package dev.betrix.superSmashMobsBrawl.brawl.passives

import dev.betrix.superSmashMobsBrawl.brawl.BrawlPassive
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.projectiles.ArrowProjectile
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.min
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.util.Vector

class BarragePassive(player: Player) : BrawlPassive("barrage", player) {

    private val maxCharge = 5
    private var charge = 0
    private var chargeRunnable: TwilightRunnable? = null

    override fun setup() {
        listeners.add(
            event<PlayerInteractEvent> {
                if (
                    player != this@BarragePassive.player ||
                        player.inventory.itemInMainHand.type != Material.BOW ||
                        !player.inventory.contains(Material.ARROW) ||
                        (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
                ) {
                    return@event
                }

                finishFiring()

                val newChargeRunnable =
                    repeatingTask(20, 6) {
                        if (charge < maxCharge) {
                            incrementCharge()
                        }
                    }

                chargeRunnable = newChargeRunnable
                runnables.add(newChargeRunnable)
            }
        )

        listeners.add(
            event<PlayerItemHeldEvent> {
                if (player != this@BarragePassive.player) {
                    return@event
                }

                val storedCharge = charge
                finishFiring()
                charge = storedCharge
                player.exp = min(0.9999F, charge.toFloat() / maxCharge.toFloat())
            }
        )

        listeners.add(
            event<EntityShootBowEvent> {
                if (projectile !is Arrow) {
                    return@event
                }

                val arrow = projectile as Arrow
                val source = arrow.shooter

                if (source != player) {
                    return@event
                }

                onBowFired()
                finishFiring()
            }
        )

        super.setup()
    }

    override fun teardown() {
        finishFiring()
        super.teardown()
    }

    private fun onBowFired() {
        repeat(charge) { idx ->
            delay((idx + 1).toLong()) {
                if (!player.isOnline) return@delay

                val spread =
                    Vector(
                        (Math.random() - 0.5) / 10,
                        (Math.random() - 0.5) / 10,
                        (Math.random() - 0.5) / 10,
                    )

                val arrow =
                    ArrowProjectile(player, "Barrage Arrow", 3.0, spread).onHitLivingEntity {
                        entity,
                        arrow ->
                        arrow.projectile?.remove()

                        SmashDamageEvent(
                                entity,
                                Damager.LivingEntity(player),
                                6.0,
                                damageType = SmashDamageType.Projectile,
                            )
                            .callEvent()

                        true
                    }

                arrow.launch()

                player.playSound(player.eyeLocation, Sound.ENTITY_ARROW_SHOOT, 1f, 1f)
            }
        }
    }

    private fun incrementCharge() {
        charge++
        player.exp = min(0.9999F, charge.toFloat() / maxCharge.toFloat())
        player.playSound(player.eyeLocation, Sound.BLOCK_DISPENSER_FAIL, 1f, 1 + 0.1f * charge)
    }

    private fun finishFiring() {
        runnables.remove(chargeRunnable)
        chargeRunnable?.cancel()
        chargeRunnable = null
        player.exp = 0f
        charge = 0
    }
}
