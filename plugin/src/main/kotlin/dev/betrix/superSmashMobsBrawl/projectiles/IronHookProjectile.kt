package dev.betrix.superSmashMobsBrawl.projectiles

import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class IronHookProjectile(owner: Player, name: String = "messages.projectiles.custom") :
    BrawlProjectile(owner, name) {

    init {
        projectileSize(0.6)
        trailEffect(Particle.CRIT, 1, Vector(0.0, 0.0, 0.0))
        addEffect(TrailSoundEffect())
    }

    override fun doVelocity() {
        projectileEntity?.setVelocity(
            owner.eyeLocation.direction,
            1.8,
            false,
            0.0,
            0.2,
            10.0,
            false,
        )
    }

    override fun createProjectileEntity(): Item =
        owner.world.dropItem(owner.eyeLocation, ItemStack.of(Material.TRIPWIRE_HOOK)).apply {}
}

class TrailSoundEffect: ProjectileEffect {

    override fun onTick(projectile: BrawlProjectile) {
        projectile.projectileEntity?.let {
            it.world.playSound(it.location, Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f)
        }
    }

    override fun onHit(projectile: BrawlProjectile, hitType: HitType) {}
}