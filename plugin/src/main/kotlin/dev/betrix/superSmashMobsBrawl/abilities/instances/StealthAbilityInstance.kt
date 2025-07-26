package dev.betrix.superSmashMobsBrawl.abilities.instances

import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class StealthAbilityInstance(definition: AbilityDefinition, player: Player) :
    AbilityInstance(definition, player) {

    private val stealthDuration = 100 // 5 seconds (100 ticks)
    private var isStealthed = false
    private var stealthTask: gg.flyte.twilight.scheduler.TwilightRunnable? = null

    override fun canActivate(): Boolean {
        return super.canActivate() && !isStealthed
    }

    override fun setup() {
        // No manual event handling needed for activation - managed by HotbarService

        // Cancel stealth when attacking or taking damage
        event<EntityDamageByEntityEvent> {
            if (damager == this@StealthAbilityInstance.player && isStealthed) {
                endStealth()
                this@StealthAbilityInstance.player.sendMessage("§7Stealth ended due to attacking!")
            }
            if (entity == this@StealthAbilityInstance.player && isStealthed) {
                endStealth()
                this@StealthAbilityInstance.player.sendMessage(
                    "§7Stealth ended due to taking damage!"
                )
            }
        }
    }

    override fun teardown() {
        if (isStealthed) {
            endStealth()
        }
    }

    override fun activate(): Boolean {
        if (!canActivate()) {
            if (isOnCooldown()) {
                player.sendMessage("§cStealth is on cooldown! (${getRemainingCooldown()}s)")
            } else if (isStealthed) {
                player.sendMessage("§cAlready stealthed!")
            }
            return false
        }

        setCooldown()
        startStealth()
        return true
    }

    private fun startStealth() {
        isStealthed = true

        // Visual and audio effects for activation
        player.world.spawnParticle(
            Particle.SMOKE,
            player.location.add(0.0, 1.0, 0.0),
            15,
            0.5,
            1.0,
            0.5,
            0.1,
        )
        player.world.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.5f)

        // Apply invisibility and speed effects
        player.addPotionEffect(
            PotionEffect(PotionEffectType.INVISIBILITY, stealthDuration, 0, false, false)
        )
        player.addPotionEffect(
            PotionEffect(PotionEffectType.SPEED, stealthDuration, 1, false, false)
        )

        player.sendMessage("§7You are now stealthed! §eDuration: §f5 seconds")

        // Particle trail while stealthed (only visible to the player)
        stealthTask =
            repeatingTask(5) {
                if (!isStealthed) {
                    this.cancel()
                    return@repeatingTask
                }

                // Subtle particle effects that only the stealthed player can see
                player.spawnParticle(
                    Particle.ENCHANTED_HIT,
                    player.location.add(0.0, 0.5, 0.0),
                    3,
                    0.3,
                    0.3,
                    0.3,
                    0.05,
                )
            }

        // Automatically end stealth after duration
        delay(stealthDuration.toLong()) {
            if (isStealthed) {
                endStealth()
                player.sendMessage("§7Stealth has ended.")
            }
        }

        // Warning when stealth is about to end
        delay((stealthDuration - 20).toLong()) { // 1 second before end
            if (isStealthed) {
                player.sendMessage("§eWarning: Stealth ending in 1 second!")
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f)
            }
        }
    }

    private fun endStealth() {
        if (!isStealthed) return

        isStealthed = false
        stealthTask?.cancel()
        stealthTask = null

        // Remove stealth effects
        player.removePotionEffect(PotionEffectType.INVISIBILITY)
        player.removePotionEffect(PotionEffectType.SPEED)

        // Visual and audio effects for deactivation
        player.world.spawnParticle(
            Particle.SMOKE,
            player.location.add(0.0, 1.0, 0.0),
            10,
            0.3,
            0.5,
            0.3,
            0.05,
        )
        player.world.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 0.8f)

        // Brief speed boost when coming out of stealth
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 40, 0, false, false))
    }
}
