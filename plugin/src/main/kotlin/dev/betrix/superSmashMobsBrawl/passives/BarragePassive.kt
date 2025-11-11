package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileAction
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

    private val maxCharge = metadata.int("maxCharge") ?: 5
    private val arrowDamage = metadata.double("arrowDamage") ?: 6.0
    private var charge = 0
    private var chargeRunnable: TwilightRunnable? = null

    private val activeProjectiles = mutableListOf<BrawlProjectile>()

    override fun setup() {
        listeners.add(
            event<PlayerInteractEvent> {
                if (
                    player != this@BarragePassive.player ||
                        player.inventory.itemInMainHand.type != Material.BOW ||
                        !(player.inventory.contains(Material.ARROW) ||
                            player.inventory.contains(Material.TIPPED_ARROW) ||
                            player.inventory.contains(Material.SPECTRAL_ARROW)) ||
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

                finishFiring()
            }
        )

        listeners.add(
            event<EntityShootBowEvent> {
                if (projectile !is Arrow) {
                    return@event
                }

                val arrow = projectile as Arrow
                val source = arrow.shooter

                if (source != player || entity != player) {
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
        activeProjectiles.forEach { it.teardown() }
        activeProjectiles.clear()
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
                    BrawlProjectile.arrow(player, 3.0, "messages.projectiles.barrage_arrow")
                        .velocityAddend(spread)
                        .onHitEntity { entity, _ ->
                            BrawlDamageEvent(
                                    entity,
                                    Damager.DamagerLivingEntity(player),
                                    arrowDamage,
                                    damageType = BrawlDamageType.Projectile,
                                )
                                .callEvent()

                            ProjectileAction.DESTROY
                        }
                        .onTeardown { activeProjectiles.remove(it) }
                        .launch()

                activeProjectiles.add(arrow)

                player.playSound(player.eyeLocation, Sound.ENTITY_ARROW_SHOOT, 1f, 1f)
            }
        }
    }

    private fun incrementCharge() {
        charge++
        val chargeProgress = min(0.9999F, charge.toFloat() / maxCharge.toFloat())
        energyManager?.setOverlay(chargeProgress, id)
        if (energyManager == null) {
            player.exp = chargeProgress
        }
        player.playSound(player.eyeLocation, Sound.BLOCK_DISPENSER_FAIL, 1f, 1 + 0.1f * charge)
    }

    private fun finishFiring() {
        chargeRunnable?.let {
            runnables.remove(it)
            it.cancel()
        }
        chargeRunnable = null
        energyManager?.clearOverlay(id)
        if (energyManager == null) {
            player.exp = 0f
        }
        charge = 0
    }
}
