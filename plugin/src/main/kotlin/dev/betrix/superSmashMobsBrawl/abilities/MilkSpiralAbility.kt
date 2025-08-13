package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class MilkSpiralAbility(player: Player) : BrawlAbility("milk_spiral", player) {

    private val durationTicks = metadata.int("durationTicks") ?: 160 // 8s
    private val propelTicks = metadata.int("propelTicks") ?: 80 // 4s
    private val helixRadius = metadata.double("helixRadius") ?: 1.5
    private val centerSpeed = metadata.double("centerSpeed") ?: 0.9
    private val playerSpeed = metadata.double("playerSpeed") ?: 1.0
    private val damage = metadata.double("damage") ?: 5.0
    private val maxTargets = metadata.int("maxTargets") ?: 2
    private val hitCooldownTicks = metadata.int("hitCooldownTicks") ?: 10

    override fun activate() {
        val dir = player.eyeLocation.direction.normalize()
        var center = player.eyeLocation.clone()
        var tick = 0
        val victims = mutableSetOf<Player>()
        val lastHitTick = mutableMapOf<Player, Int>()
        var theta = 0.0

        runnables.add(
            repeatingTask(1) {
                if (tick >= durationTicks || !player.isOnline) {
                    cancel()
                    return@repeatingTask
                }

                // Move spiral center forward
                center = center.add(dir.clone().multiply(centerSpeed))

                // Propel player for first 4s unless sneaking
                if (tick < propelTicks && !player.isSneaking) {
                    player.velocity = dir.clone().multiply(playerSpeed)
                }

                // Build orthonormal basis perpendicular to dir
                val up = Vector(0.0, 1.0, 0.0)
                var right = dir.clone().crossProduct(up)
                if (right.lengthSquared() < 1e-6) {
                    right = Vector(1.0, 0.0, 0.0)
                }
                right.normalize()
                val upOrtho = right.clone().crossProduct(dir).normalize()

                // Two helix points
                val p1 =
                    center
                        .clone()
                        .add(right.clone().multiply(cos(theta) * helixRadius))
                        .add(upOrtho.clone().multiply(sin(theta) * helixRadius))
                val p2 =
                    center
                        .clone()
                        .add(right.clone().multiply(cos(theta + PI) * helixRadius))
                        .add(upOrtho.clone().multiply(sin(theta + PI) * helixRadius))

                // Particles
                center.world.spawnParticle(Particle.CLOUD, p1, 3, 0.05, 0.05, 0.05, 0.0)
                center.world.spawnParticle(Particle.CLOUD, p2, 3, 0.05, 0.05, 0.05, 0.0)
                if (tick % 6 == 0) {
                    center.world.playSound(center, Sound.ENTITY_COW_MILK, 0.5f, 1.2f)
                }

                // Damage detection near helix points
                fun damageAt(point: Location) {
                    if (victims.size >= maxTargets) return
                    val nearby = point.world.getNearbyPlayers(point, 0.9)
                    nearby.forEach { target ->
                        if (target == player) return@forEach
                        if (victims.size >= maxTargets) return@forEach
                        val last = lastHitTick[target] ?: -9999
                        if (tick - last < hitCooldownTicks) return@forEach

                        SmashDamageEvent(target, Damager.DamagerLivingEntity(player), damage)
                            .callEvent()
                        lastHitTick[target] = tick
                        victims.add(target)
                    }
                }

                damageAt(p1)
                damageAt(p2)

                theta += 0.45
                tick++
            }
        )

        player.playSound(player.location, Sound.ITEM_BUCKET_FILL, 1f, 1.2f)
        super.activate()
    }
}
