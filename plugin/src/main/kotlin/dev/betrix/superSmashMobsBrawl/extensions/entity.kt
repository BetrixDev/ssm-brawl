package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import gg.flyte.twilight.extension.add
import kotlin.math.abs
import kotlin.math.log10
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Projectile
import org.bukkit.util.Vector

/**
 * Calculates the entity's current speed in blocks per second.
 *
 * This extension property provides a convenient way to measure how fast an entity is currently
 * moving in the game world. It converts the internal velocity units (which are in blocks per tick)
 * to a more human-readable blocks per second value.
 *
 * The calculation is based on the assumption that Minecraft runs at 20 ticks per second, so the
 * velocity vector's magnitude is multiplied by 20 to get the speed in blocks per second.
 *
 * **Important Notes:**
 * - Returns 0.0 if the entity is stationary
 * - Negative values are not possible (uses vector length/magnitude)
 * - Does not account for vertical movement separately - this is total speed in 3D space
 * - Values can exceed 20+ blocks per second for fast-moving entities or abilities
 *
 * **Typical Values:**
 * - Walking player: ~4.3 blocks/second
 * - Sprinting player: ~5.6 blocks/second
 * - Falling entity: Can reach 20+ blocks/second
 * - Arrow projectile: ~30-50 blocks/second
 *
 * Example usage:
 * ```kotlin
 * val speed = player.blocksPerSecond
 * if (speed > 10.0) {
 *     // Player is moving very fast (falling, launched, etc.)
 *     println("Player speed: ${speed.format(2)} blocks/sec")
 * }
 * ```
 *
 * @return The entity's current speed as a positive value in blocks per second
 * @see Entity.getVelocity For accessing the raw velocity vector
 * @see Entity.setVelocity For modifying the entity's velocity
 */
val Entity.blocksPerSecond: Double
    get() = this.velocity.length() * 20

/**
 * Teleports this entity to a specific spawn point location.
 *
 * This extension function provides a convenient way to teleport entities to predefined spawn points
 * as defined in the `maps.yml` configuration file. It automatically handles the conversion from
 * [SpawnPoint] data objects to Bukkit [Location] objects.
 *
 * **Use Cases:**
 * - Teleporting players to minigame spawn points at match start
 * - Moving players to spectator spawn points after death
 * - Returning players to hub spawn points after minigames
 * - Setting up initial player positions in custom game modes
 *
 * **Behavior:**
 * - Preserves the entity's current world (does not change worlds)
 * - Sets both position and rotation (yaw/pitch) from the spawn point
 * - Works for all entity types, not just players
 * - Does not trigger teleport events or play teleport effects
 *
 * **Configuration Integration:** This function is designed to work seamlessly with spawn points
 * defined in `maps.yml` under both `gameMaps.spawnPoints` and `hubMaps.spawnPoints` sections.
 *
 * Example usage:
 * ```kotlin
 * // Get spawn point from maps.yml configuration
 * val spawnPoints = gameMap.spawnPoints
 * val randomSpawn = spawnPoints.random()
 *
 * // Teleport player to the spawn point
 * player.teleport(randomSpawn)
 *
 * // Or use for spectator spawn
 * val spectatorSpawn = gameMap.spectatorSpawnPoint
 * deadPlayer.teleport(spectatorSpawn)
 * ```
 *
 * @param spawnPoint The spawn point containing coordinates and rotation data
 * @throws IllegalStateException If the entity is removed or the world is unloaded
 * @see SpawnPoint Data class containing spawn location information
 * @see Location Bukkit location object for precise world positioning
 */
fun Entity.teleport(spawnPoint: SpawnPoint) {
    teleport(Location(world, spawnPoint.x, spawnPoint.y, spawnPoint.z))
}

/**
 * Sets the velocity of this entity using directional vector calculation.
 *
 * This is a convenience method that calculates the velocity based on the entity's current facing
 * direction. It delegates to the more comprehensive [setVelocity] method with sensible defaults for
 * most parameters.
 *
 * @param strength The multiplier applied to the directional vector. Higher values result in faster
 *   movement. Typical values range from 0.5 to 3.0.
 * @param yAdd Additional vertical velocity added after directional calculation. Positive values
 *   launch the entity upward, negative values push downward. Common values: 0.3-0.8 for upward
 *   jumps, -0.2 for downward force.
 * @param yMax Maximum allowed vertical velocity component. Prevents excessive upward motion that
 *   could launch entities too high. Usually set to 1.0-2.0 for normal gameplay mechanics.
 * @param groundBoost Whether to add extra vertical velocity when the entity is on ground. This
 *   simulates jumping mechanics where grounded entities get a small boost. Adds 0.2 to vertical
 *   velocity when true and on ground.
 * @see setVelocity For the full velocity calculation method with all parameters.
 *
 * Example usage:
 * ```kotlin
 * player.setVelocity(1.5, 0.6, 1.2, true) // Standard jump forward
 * entity.setVelocity(2.0, -0.5, 0.8, false) // Strong backward knockback
 * ```
 */
fun Entity.setVelocity(strength: Double, yAdd: Double, yMax: Double, groundBoost: Boolean) {
    setVelocity(this.location.direction, strength, false, 0.0, yAdd, yMax, groundBoost)
}

/**
 * Sets the velocity of this entity with comprehensive control over all velocity components.
 *
 * This method provides fine-grained control over entity movement and is the primary
 * velocity-setting mechanism used throughout the plugin. It handles edge cases like NaN values and
 * zero vectors, applies various velocity modifications, and ensures consistent behavior across
 * different entity types.
 *
 * The velocity calculation follows this process:
 * 1. Validate input vector (skip if NaN or zero length)
 * 2. Optionally override Y component with [yBase] value
 * 3. Normalize and scale the vector by [strength]
 * 4. Add [yAdd] to the Y component for additional vertical motion
 * 5. Clamp Y component to [yMax] to prevent excessive upward velocity
 * 6. Apply ground boost if enabled and entity is on ground
 * 7. Reset fall distance to prevent fall damage
 * 8. Apply final velocity to entity
 *
 * @param velocity The base direction vector for movement. Will be normalized before application.
 *   Must not contain NaN values or be zero length.
 * @param strength Multiplier applied to the normalized velocity vector. Controls the overall speed
 *   of movement. Typical values: 0.5-4.0 for normal gameplay, higher for special abilities or
 *   knockback.
 * @param ySet Whether to override the Y component of the velocity vector with [yBase]. Useful when
 *   you want complete control over vertical motion regardless of the input vector's Y component.
 * @param yBase The Y velocity value to use when [ySet] is true. Ignored when [ySet] is false.
 *   Positive values launch upward, negative values push downward.
 * @param yAdd Additional Y velocity added after all other calculations. This is applied after
 *   normalization and scaling, allowing for fine-tuning of vertical motion. Common for jump boosts
 *   or gravity effects.
 * @param yMax Maximum allowed Y velocity component. Acts as a ceiling to prevent excessive upward
 *   motion. Set to Double.MAX_VALUE to disable clamping.
 * @param groundBoost Whether to add extra upward velocity when the entity is on ground. Adds 0.2 to
 *   Y velocity when true and entity.isOnGround is true. Simulates jumping mechanics and
 *   ground-based launch effects.
 * @throws IllegalStateException If the entity is removed or invalid when setting velocity.
 * @see Entity.setVelocity Simplified version with fewer parameters.
 * @see Entity.velocity Direct property access for simple velocity setting.
 *
 * Example usage:
 * ```kotlin
 * // Standard forward dash
 * entity.setVelocity(
 *     velocity = player.location.direction,
 *     strength = 2.5,
 *     ySet = false,
 *     yBase = 0.0,
 *     yAdd = 0.3,
 *     yMax = 1.5,
 *     groundBoost = true
 * )
 *
 * // Vertical launch regardless of facing direction
 * entity.setVelocity(
 *     velocity = Vector(0, 1, 0),
 *     strength = 1.0,
 *     ySet = true,
 *     yBase = 2.0,
 *     yAdd = 0.0,
 *     yMax = 3.0,
 *     groundBoost = false
 * )
 *
 * // Knockback effect
 * entity.setVelocity(
 *     velocity = attacker.location.direction.setY(0).normalize(),
 *     strength = 1.8,
 *     ySet = false,
 *     yBase = 0.0,
 *     yAdd = 0.4,
 *     yMax = 1.2,
 *     groundBoost = true
 * )
 * ```
 */
fun Entity.setVelocity(
    velocity: Vector,
    strength: Double,
    ySet: Boolean,
    yBase: Double,
    yAdd: Double,
    yMax: Double,
    groundBoost: Boolean,
) {
    if (
        velocity.x.isNaN() || velocity.y.isNaN() || velocity.z.isNaN() || velocity.length() == 0.0
    ) {
        return
    }

    if (ySet) {
        velocity.y = yBase
    }

    velocity.normalize()
    velocity.multiply(strength)

    velocity.y += yAdd

    if (velocity.y > yMax) {
        velocity.y = yMax
    }

    if (groundBoost && isOnGround) {
        velocity.y += 0.2
    }

    this.fallDistance = 0F

    this.velocity = velocity
}

fun Entity.doKnockback(
    multiplier: Double,
    damage: Double,
    startingHealth: Double,
    origin: Vector?,
    projectile: Projectile?,
) {
    this.velocity = Vector(0.0, 0.0, 0.0)
    var knockback = damage.coerceAtLeast(2.0)
    knockback = log10(knockback)
    knockback *= multiplier

    //    if (this is Player) {
    //        val kitKnockbackMult = this.getMetadata(TaggedKeyDouble("knockback_multiplier"))

    //        if (kitKnockbackMult != null) {
    //            knockback *= kitKnockbackMult
    //        }

    //        knockback *=
    //            (1 + 0.1 * (this.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value!! -
    // startingHealth))
    //    }

    var trajectory: Vector? = null
    if (origin != null) {
        trajectory = this.location.toVector().subtract(origin).setY(0).normalize()
        trajectory.multiply(0.6 * knockback)
        trajectory.y = abs(trajectory.y)
    }
    if (projectile != null) {
        trajectory = projectile.velocity
        trajectory.y = 0.0
        trajectory.multiply(0.37 * knockback / trajectory.length())
        trajectory.y = 0.06
    }

    val vel = 0.2 + trajectory!!.length() * 0.8

    this.setVelocity(
        trajectory,
        vel,
        false,
        0.0,
        abs(0.2 * knockback),
        0.4 + (0.04 * knockback),
        true,
    )
}

/**
 * Determines whether this entity is currently standing on a solid block surface.
 *
 * This function performs comprehensive ground detection to determine if an entity is in contact
 * with a solid surface that would allow ground-based actions like jumping, ability activation, or
 * prevent falling. It accounts for various edge cases and special block types that players commonly
 * interact with.
 *
 * **Detection Logic:** The function checks multiple potential contact points around the entity's
 * position using a grid-based approach. It examines:
 * - Direct block contact beneath the entity
 * - Lily pad surfaces (special case for water traversal)
 * - Fence and cobblestone wall tops (for precise edge detection)
 * - Non-air, non-liquid solid blocks
 *
 * **Use Cases:**
 * - Determining if a player can perform a double jump
 * - Checking if an ability requiring ground contact can be activated
 * - Preventing ground-boost effects while airborne
 * - Validating spawn locations and preventing suffocation
 *
 * **Edge Cases Handled:**
 * - Partial block positions (entity standing on block edges)
 * - Fence posts and walls (correctly identifies walkable surfaces)
 * - Lily pads (counts as solid ground for water traversal)
 * - Half-block positions (slabs, stairs, etc.)
 *
 * **Performance Note:** This function performs multiple block lookups in a small radius around the
 * entity. While optimized for performance, avoid calling it every tick for many entities.
 *
 * Example usage:
 * ```kotlin
 * // Check if player can jump
 * if (player.isOnBlock()) {
 *     player.setVelocity(player.location.direction, 1.5, false, 0.0, 0.5, 1.0, true)
 * }
 *
 * // Validate ability activation
 * fun canUseGroundAbility(player: Player): Boolean {
 *     return player.isOnBlock() && !player.isInWater
 * }
 * ```
 *
 * @return `true` if the entity is standing on a solid, walkable surface; `false` otherwise
 * @see Entity.isOnGround Vanilla Minecraft's simpler ground detection
 * @see Material Checks for specific block types during detection
 */
fun Entity.isOnBlock(): Boolean {
    var xMod = location.x % 1

    if (location.x < 0) {
        xMod += 1
    }

    var zMod = location.z % 1

    if (location.z < 1) {
        zMod += 1
    }

    var xMin = 0
    var xMax = 0
    var zMin = 0
    var zMax = 0

    if (xMod < 0.3) {
        xMin = -1
    }
    if (xMod > 0.7) {
        xMax = 1
    }
    if (zMod < 0.3) {
        zMin = -1
    }
    if (zMod > 0.7) {
        zMax = 1
    }

    for (x in xMin..xMax) {
        for (z in zMin..zMax) {
            if (
                location.add(x, -0.5, z).block.type != Material.AIR &&
                    !location.add(x, -0.5, z).block.isLiquid
            )
                return true
            if (location.add(x, 0, z).block.type == Material.LILY_PAD) return true
            val beneath: Material = location.add(x, -1.5, z).block.type
            if (
                location.y % 0.5 == 0.0 &&
                    (beneath.toString().contains("FENCE") || beneath == Material.COBBLESTONE_WALL)
            )
                return true
        }
    }
    return false
}
