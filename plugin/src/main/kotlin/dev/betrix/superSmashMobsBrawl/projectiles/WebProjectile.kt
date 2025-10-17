package dev.betrix.superSmashMobsBrawl.projectiles

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import gg.flyte.twilight.scheduler.delay
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import org.koin.core.component.inject

class WebProjectile(
    owner: Player,
    name: String,
    private val webDamage: Double = 4.0,
    private val knockbackMult: Double = 0.0,
    private val webBlockDurationTicks: Long = 40L,
    private val poisonDurationTicks: Int = 40,
    private val poisonAmplifier: Int = 0,
) : BrawlProjectile(owner, name) {

    private val minigameService: MinigameService by inject()
    private val trackedBlocks = mutableMapOf<Block, Long>()

    init {
        projectileSize(0.5)
        maxLifetime(2000L)
    }

    override fun createProjectileEntity(): Item =
        owner.world.spawn(owner.location.clone().add(0.0, 0.5, 0.0), Item::class.java)
            .apply {
                itemStack = ItemStack(Material.COBWEB, 1)
                pickupDelay = Int.MAX_VALUE
            }

    override fun doVelocity() {
        val entity = projectileEntity ?: return

        val spread = Vector(
            (Math.random() - 0.5) * 2,
            (Math.random() - 0.5) * 2,
            (Math.random() - 0.5) * 2
        ).normalize()

        val backwardDirection = owner.location.direction.multiply(-1)
        val finalVelocity = backwardDirection.clone().add(spread.multiply(0.2))

        entity.setVelocity(
            finalVelocity,
            0.5 + Math.random() * 0.4,
            false,
            0.0,
            0.2,
            10.0,
            false
        )
    }

    fun setupHitCallbacks(): WebProjectile = apply {
        onHitEntity { entity, _ ->
            if (entity == owner) {
                return@onHitEntity ProjectileAction.CONTINUE
            }

            val minigame = minigameService.getMinigameForPlayer(owner)
            if (entity is Player && minigame != null) {
                if (minigame.arePlayersOnSameTeam(owner, entity)) {
                    return@onHitEntity ProjectileAction.CONTINUE
                }
            }

            // Apply damage
            BrawlDamageEvent(
                entity,
                Damager.DamagerLivingEntity(owner),
                webDamage,
                knockbackMult,
                BrawlDamageType.Projectile,
            ).callEvent()

            // Apply poison effect
            if (poisonDurationTicks > 0) {
                entity.removePotionEffect(PotionEffectType.POISON)
                entity.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.POISON,
                        poisonDurationTicks,
                        poisonAmplifier,
                    )
                )
            }

            // Create web block at hit location
            createWebBlock(entity.location)

            ProjectileAction.DESTROY
        }

        onHitBlock { block, _ ->
            createWebBlock(block.location)
            ProjectileAction.DESTROY
        }

        onExpire { _ ->
            projectileEntity?.location?.let { createWebBlock(it) }
            ProjectileAction.DESTROY
        }
    }

    private fun createWebBlock(location: Location) {
        val blockLocation = location.block.location
        val block = blockLocation.block

        if (
            block.type == Material.AIR ||
            block.type == Material.CAVE_AIR ||
            block.type == Material.VOID_AIR
        ) {
            if (!trackedBlocks.containsKey(block)) {
                trackedBlocks[block] = System.currentTimeMillis()

                block.type = Material.COBWEB

                val delayTicks = (webBlockDurationTicks).coerceAtLeast(1L)
                delay(delayTicks) {
                    if (block.type == Material.COBWEB) {
                        block.type = Material.AIR
                    }
                    trackedBlocks.remove(block)
                }
            }
        }
    }

    override fun teardown() {
        trackedBlocks.clear()
        super.teardown()
    }
}
