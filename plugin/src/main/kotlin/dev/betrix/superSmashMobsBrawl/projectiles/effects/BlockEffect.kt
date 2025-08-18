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
        val entity = projectile.projectileEntity ?: return
        val ticksLived = entity.ticksLived
        if (ticksLived < 2) return

        val particleCount = ticksLived.coerceAtMost(5)

        Particle.BLOCK_CRUMBLE.builder()
            .location(entity.location.addY(entity.height / 2))
            .count(particleCount)
            .offset(0.1, 0.1, 0.1)
            .extra(0.1)
            .data(blockData)
            .receivers(96, true)
            .spawn()
    }

    override fun onHit(projectile: BrawlProjectile, hitType: HitType) {
        val entity = projectile.projectileEntity ?: return
        entity.world.playSound(
            entity.location,
            blockData.soundGroup.breakSound,
            SoundCategory.BLOCKS,
            1f,
            1f,
        )

        Particle.BLOCK_CRUMBLE.builder()
            .location(entity.location.addY(entity.height / 2))
            .count(50)
            .offset(0.75, 0.75, 0.75)
            .extra(0.25)
            .data(blockData)
            .receivers(96, true)
            .spawn()
    }
}
