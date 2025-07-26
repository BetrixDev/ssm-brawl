package dev.betrix.superSmashMobsBrawl.abilities.instances

import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.scheduler.delay
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.RayTraceResult

class LightningJumpAbilityInstance(definition: AbilityDefinition, player: Player) :
    AbilityInstance(definition, player) {

    private val maxRange = 20.0
    private val damage = 6.0
    private val knockbackMultiplier = 1.8
    private val effectRadius = 4.0

    override fun canActivate(): Boolean {
        return super.canActivate() && isOnGround(player)
    }

    override fun setup() {
        // No manual event handling needed - managed by HotbarService
    }

    override fun teardown() {
        // Event listeners are automatically cleaned up by Twilight
    }

    override fun activate(): Boolean {
        if (!canActivate()) {
            if (isOnCooldown()) {
                player.sendMessage("§cLightning Jump is on cooldown! (${getRemainingCooldown()}s)")
            }
            return false
        }

        val targetLocation = getTargetLocation()
        if (targetLocation == null) {
            player.sendMessage("§cNo valid teleport destination found!")
            return false
        }

        setCooldown()
        performLightningJump(targetLocation)
        return true
    }

    private fun getTargetLocation(): Location? {
        val rayTrace: RayTraceResult? = player.rayTraceBlocks(maxRange)

        if (rayTrace?.hitBlock == null) {
            // If no block hit, use max range in the direction player is looking
            val direction = player.location.direction
            val targetLocation = player.location.add(direction.multiply(maxRange))

            // Find the highest solid block at this location
            val world = player.world
            val x = targetLocation.blockX
            val z = targetLocation.blockZ

            for (y in world.maxHeight downTo world.minHeight) {
                val block = world.getBlockAt(x, y, z)
                if (block.type.isSolid) {
                    return Location(
                        world,
                        x.toDouble() + 0.5,
                        y.toDouble() + 1.1,
                        z.toDouble() + 0.5,
                    )
                }
            }
            return null
        }

        val hitBlock: Block = rayTrace.hitBlock!!
        val location = hitBlock.location.add(0.5, 1.1, 0.5)

        // Ensure the location is safe to teleport to
        if (!location.block.type.isAir || !location.add(0.0, 1.0, 0.0).block.type.isAir) {
            // Try to find a safe location above
            for (i in 1..5) {
                val testLocation = location.clone().add(0.0, i.toDouble(), 0.0)
                if (
                    testLocation.block.type.isAir &&
                        testLocation.add(0.0, 1.0, 0.0).block.type.isAir
                ) {
                    return testLocation
                }
            }
            return null
        }

        return location
    }

    private fun performLightningJump(targetLocation: Location) {
        val originalLocation = player.location.clone()

        // Play departure effects
        originalLocation.world?.spawnParticle(
            Particle.ELECTRIC_SPARK,
            originalLocation,
            15,
            0.5,
            1.0,
            0.5,
            0.1,
        )
        originalLocation.world?.playSound(originalLocation, Sound.ENTITY_CREEPER_PRIMED, 1.0f, 2.0f)

        // Teleport player
        player.teleport(targetLocation)

        // Delay slightly for arrival effects
        delay(2) {
            // Lightning strike visual effect (without damage to blocks)
            targetLocation.world?.spawnParticle(
                Particle.ELECTRIC_SPARK,
                targetLocation,
                30,
                1.0,
                2.0,
                1.0,
                0.2,
            )
            targetLocation.world?.spawnParticle(Particle.FLASH, targetLocation, 1)
            targetLocation.world?.playSound(
                targetLocation,
                Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                1.0f,
                1.2f,
            )
            targetLocation.world?.playSound(
                targetLocation,
                Sound.ENTITY_LIGHTNING_BOLT_IMPACT,
                1.0f,
                1.0f,
            )

            // Damage nearby enemies
            damageNearbyEnemies(targetLocation)
        }

        player.sendMessage("§eLightning Jump activated!")
    }

    private fun damageNearbyEnemies(location: Location) {
        val nearbyPlayers =
            location.world
                ?.getNearbyEntities(location, effectRadius, effectRadius, effectRadius)
                ?.filterIsInstance<Player>()
                ?.filter { it != player }

        nearbyPlayers?.forEach { target ->
            val distance = target.location.distance(location)
            if (distance <= effectRadius) {
                val damageMultiplier = 1.0 - (distance / effectRadius) * 0.3
                val finalDamage = damage * damageMultiplier.coerceAtLeast(0.4)
                val finalKnockback = knockbackMultiplier * damageMultiplier

                target.doKnockback(
                    finalKnockback,
                    finalDamage,
                    target.health,
                    location.toVector(),
                    null,
                )
                target.damage(finalDamage, player)

                // Lightning effect on hit targets
                target.world.spawnParticle(
                    Particle.ELECTRIC_SPARK,
                    target.location.add(0.0, 1.0, 0.0),
                    10,
                    0.3,
                    0.5,
                    0.3,
                    0.1,
                )

                player.sendMessage(
                    "§6Lightning struck ${target.name} for §e${finalDamage.toInt()} §6damage!"
                )
            }
        }
    }
}
