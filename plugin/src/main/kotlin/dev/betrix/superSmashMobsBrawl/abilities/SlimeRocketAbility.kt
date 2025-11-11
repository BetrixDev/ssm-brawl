package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.projectiles.SlimeProjectile
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class SlimeRocketAbility(player: Player) : BrawlAbility("slime_rocket", player) {

    private var chargingTask: TwilightRunnable? = null
    private var chargeStartTimeMs: Long = 0L
    private var isCharging = false
    private var currentFuel: Float = 1.0f

    private val maxChargeTimeMs = metadata.long("maxChargeTimeMs") ?: 3000L
    private val maxChargeDurationMs = metadata.long("maxChargeDurationMs") ?: 5000L
    private val expDrainPerTick = metadata.float("expDrainPerTick") ?: 0.00916f
    private val minExpToCharge = metadata.float("minExpToCharge") ?: 0.1f

    override fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != null && event.hand != EquipmentSlot.HAND) return

        if (!isCorrectActionForUsage(event.action)) {
            return
        }

        val item = event.item ?: return

        if (!isCorrectItemForAbility(item)) {
            return
        }

        event.isCancelled = true

        // If already charging, don't re-activate
        if (isCharging) {
            return
        }

        if (!canActivate()) {
            return
        }

        startCharging()
    }

    private fun startCharging() {
        isCharging = true
        chargeStartTimeMs = System.currentTimeMillis()
        currentFuel = 1.0f

        // Call activate to set cooldown and send message
        activate()

        // Start charging loop
        chargingTask?.cancel()
        chargingTask =
            repeatingTask(0, 0) {
                if (!player.isValid || !player.isOnline) {
                    stopCharging(true)
                    return@repeatingTask
                }

                // Check if player stopped blocking - fire rocket
                if (!player.isBlocking) {
                    stopCharging(false)
                    fireRocket()
                    return@repeatingTask
                }

                val elapsedMs = System.currentTimeMillis() - chargeStartTimeMs
                val elapsedSeconds = (elapsedMs / 1000.0).coerceAtMost(3.0)

                // Drain fuel only during the first 3 seconds
                if (elapsedMs < maxChargeTimeMs) {
                    currentFuel = (currentFuel - expDrainPerTick).coerceAtLeast(0f)
                    energyManager?.setOverlay(currentFuel, id)
                    if (energyManager == null) {
                        player.exp = currentFuel
                    }
                }

                // Check if fuel is too low or max duration reached
                if (
                    currentFuel < minExpToCharge ||
                        elapsedMs >= maxChargeDurationMs
                ) {
                    stopCharging(false)
                    fireRocket()
                    return@repeatingTask
                }

                // Play charging sound
                val soundPitch =
                    (0.5f + 1.5f * (elapsedSeconds / 3.0)).toFloat()
                player.world.playSound(
                    player.location,
                    Sound.ENTITY_SLIME_SQUISH,
                    0.5f,
                    soundPitch,
                )

                // Play charging particles
                val particleCount = (elapsedSeconds * 5).toInt()
                val particleSpread = (elapsedSeconds / 6.0).toFloat()

                player.world.spawnParticle(
                    Particle.ITEM_SLIME,
                    player.location.clone().add(0.0, 1.0, 0.0),
                    particleCount,
                    particleSpread.toDouble(),
                    particleSpread.toDouble(),
                    particleSpread.toDouble(),
                    0.0,
                )
            }
    }

    private fun stopCharging(cancelled: Boolean) {
        isCharging = false
        chargingTask?.cancel()
        chargingTask = null
        energyManager?.clearOverlay(id)
        currentFuel = 1.0f

        if (!cancelled) {
            player.sendMessage(
                lang.t("messages.abilities.slime_rocket.released") {
                    "abilityId" to id
                }
            )
        }
    }

    private fun fireRocket() {
        val elapsedMs = System.currentTimeMillis() - chargeStartTimeMs
        val charge = (elapsedMs / 1000.0).coerceAtMost(3.0)

        val projectile = SlimeProjectile(player, "abilities.$id.name", charge)
        projectile.setupHitCallback().launch()
    }

    override fun teardown() {
        stopCharging(true)
        super.teardown()
    }
}
