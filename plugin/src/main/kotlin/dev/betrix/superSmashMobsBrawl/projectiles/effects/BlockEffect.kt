package dev.betrix.superSmashMobsBrawl.projectiles.effects

import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.HitType
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileEffect
import gg.flyte.twilight.extension.addY
import org.bukkit.Particle
import org.bukkit.SoundCategory
import org.bukkit.block.data.BlockData

class BlockEffect(private val blockData: BlockData) : ProjectileEffect {

    override fun onTick(projectile: BrawlProjectile) {
        val ticksLived = projectile.projectileEntity?.ticksLived ?: 0

        if (ticksLived < 2) {
            return
        }

        val particleCount = Math.min(ticksLived, 5)

        Particle.BLOCK_CRUMBLE.builder()
            .location(
                projectile.projectileEntity!!
                    .location
                    .addY(projectile.projectileEntity!!.height / 2)
            )
            .count(particleCount)
            .offset(0.1, 0.1, 0.1)
            .extra(0.1)
            .data(blockData)
            .receivers(96, true)
            .spawn()
    }

    override fun onHit(projectile: BrawlProjectile, hitType: HitType) {
        projectile.projectileEntity
            ?.world
            ?.playSound(
                projectile.projectileEntity!!.location,
                blockData.soundGroup.breakSound,
                SoundCategory.BLOCKS,
                1f,
                1f,
            )

        Particle.BLOCK_CRUMBLE.builder()
            .location(
                projectile.projectileEntity!!
                    .location
                    .addY(projectile.projectileEntity!!.height / 2)
            )
            .count(50)
            .offset(0.75, 0.75, 0.75)
            .extra(0.25)
            .data(blockData)
            .receivers(96, true)
            .spawn()
    }
}
