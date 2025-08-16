package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.getLocationInFrontOfEyes
import dev.betrix.superSmashMobsBrawl.extensions.interpolateTo
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player

class BlinkAbility(player: Player) : BrawlAbility("blink", player) {

    private val teleportDistance = metadata.double("teleportDistance")!!

    override fun activate() {
        super.activate()

        val startLocation = player.eyeLocation.clone()
        val endLocation = player.getLocationInFrontOfEyes(teleportDistance)

        doTpEffect(startLocation)
        doTpEffect(endLocation)

        startLocation.interpolateTo(endLocation, 0.25).forEach { location ->
            Particle.DUST.builder()
                .count(2)
                .color(Color.fromRGB(55, 37, 40))
                .location(location)
                .receivers(96, true)
                .spawn()
        }

        Particle.PORTAL.builder()
            .location(endLocation)
            .offset(-2.0, 0.0, 2.0)
            .count(10)
            .receivers(96, true)
            .spawn()

        player.teleport(endLocation)
    }

    private fun doTpEffect(location: Location) {
        Particle.DUST_COLOR_TRANSITION.builder()
            .location(location)
            .offset(1.0, 1.0, 1.0)
            .count(100)
            .colorTransition(Color.fromRGB(138, 0, 196), Color.fromRGB(55, 37, 40))
            .receivers(96, true)
            .spawn()

        location.world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f)
    }
}
