package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.playSound
import dev.betrix.superSmashMobsBrawl.projectiles.WebProjectile
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class SpinWebAbility(player: Player) : BrawlAbility("spin_web", player) {

    private val webCount = metadata.int("webCount") ?: 25
    private val webDamage = metadata.double("webDamage") ?: 4.0
    private val webKnockbackMultiplier = metadata.double("webKnockbackMultiplier") ?: 0.0
    private val webHitboxSize = metadata.double("webHitboxSize") ?: 0.5
    private val webBlockDurationTicks = metadata.long("webBlockDurationTicks") ?: 40L
    private val launchVelocityStrength = metadata.double("launchVelocityStrength") ?: 1.2
    private val launchVelocityY = metadata.double("launchVelocityY") ?: 0.2
    private val poisonDurationTicks = metadata.int("poisonDurationTicks") ?: 40
    private val poisonAmplifier = metadata.int("poisonAmplifier") ?: 0

    private val activeProjectiles = mutableListOf<WebProjectile>()

    override fun activate() {
        super.activate()
        launchWebs()
        launchPlayer()
    }

    override fun teardown() {
        activeProjectiles.forEach { it.teardown() }
        activeProjectiles.clear()
        super.teardown()
    }

    private fun launchWebs() {
        player.playSound(Sound.ENTITY_SPIDER_AMBIENT, volume = 2f, pitch = 0.6f)

        repeat(webCount) {
            val web = WebProjectile(
                player,
                "abilities.spin_web.name",
                webDamage,
                webKnockbackMultiplier,
                webBlockDurationTicks,
                poisonDurationTicks,
                poisonAmplifier,
            )
                .projectileSize(webHitboxSize)
                .onTeardown { activeProjectiles.remove(it) }
                .launch()

            activeProjectiles.add(web as WebProjectile)
        }
    }

    private fun launchPlayer() {
        // Launch player forward
        val forwardDirection = player.location.direction
        player.velocity = Vector(
            forwardDirection.x * launchVelocityStrength,
            launchVelocityY,
            forwardDirection.z * launchVelocityStrength,
        )
    }
}
