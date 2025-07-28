package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent

class RegenerationInstance(
    definition: PassiveDefinition,
    player: Player,
    private val regenerationRate: Double,
    private val maxHealth: Double,
    private val delayTicks: Int
) : PassiveInstance(definition, player) {

    private var regenerationTask: TwilightRunnable? = null
    private var lastDamageTime: Long = 0
    private var isActive = false

    override fun setup() {
        // Set player's max health using attribute system
        player.getAttribute(Attribute.MAX_HEALTH)?.baseValue = maxHealth
        
        // Start regeneration task
        startRegenerationTask()

        // Listen for damage to reset regeneration delay
        event<EntityDamageEvent> DamageEvent@{
            if (entity != this@RegenerationInstance.player) {
                return@DamageEvent
            }

            // Reset regeneration delay when player takes damage
            lastDamageTime = System.currentTimeMillis()
        }

        // Clean up on death
        event<PlayerDeathEvent> DeathEvent@{
            if (player != this@RegenerationInstance.player) {
                return@DeathEvent
            }

            // Reset health to max on death (handled by respawn)
            lastDamageTime = System.currentTimeMillis()
        }

        // Clean up on quit
        event<PlayerQuitEvent> QuitEvent@{
            if (player != this@RegenerationInstance.player) {
                return@QuitEvent
            }

            teardown()
        }

        isActive = true
    }

    private fun startRegenerationTask() {
        regenerationTask = repeatingTask(20) { // Run every second (20 ticks)
            if (!isActive || !player.isOnline) {
                return@repeatingTask
            }

            val currentTime = System.currentTimeMillis()
            val timeSinceLastDamage = currentTime - lastDamageTime

            // Check if enough time has passed since last damage (delayTicks * 50ms per tick)
            if (timeSinceLastDamage >= delayTicks * 50) {
                regenerateHealth()
            }
        }
    }

    private fun regenerateHealth() {
        val currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        if (player.health < currentMaxHealth) {
            val newHealth = (player.health + (regenerationRate / 5.0)).coerceAtMost(currentMaxHealth)
            player.health = newHealth
        }
    }

    override fun teardown() {
        isActive = false
        regenerationTask?.cancel()
        regenerationTask = null
        
        // Reset player health to default using attribute system
        player.getAttribute(Attribute.MAX_HEALTH)?.baseValue = 20.0
        if (player.health > 20.0) {
            player.health = 20.0
        }
    }
}