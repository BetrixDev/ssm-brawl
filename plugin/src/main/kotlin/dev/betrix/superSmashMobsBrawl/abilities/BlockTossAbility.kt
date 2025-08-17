package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.disguises.EndermanDisguise
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.extensions.disguise
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.HitType
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileAction
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileEffect
import gg.flyte.twilight.extension.addY
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.*
import org.bukkit.block.data.BlockData
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class BlockTossAbility(player: Player) : BrawlAbility("block_toss", player) {

    private var holdingBlockData: BlockData? = null
    private var pickupTimeMs: Long = 0
    private var tossRunnable: TwilightRunnable? = null
    private val chargeTimeMs = metadata.long("chargeTimeMs") ?: 1200L
    private val maxDamage = metadata.double("maxDamage") ?: 9.0
    private val damage = metadata.double("damage") ?: 8.0
    private val knockbackMultiplier = metadata.double("knockbackMultiplier") ?: 2.5

    override fun setup() {
        super.setup()
    }

    override fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != null && event.hand != EquipmentSlot.HAND) return

        if (!isCorrectActionForUsage(event.action)) {
            return
        }

        val item = event.item ?: return

        if (!isCorrectItemForAbility(item)) {
            return
        }

        if (!canActivate()) {
            return
        }

        val block = event.clickedBlock ?: return
        val material = block.type

        if (!material.isSolid) {
            return
        }

        setDisguiseBlock(material)
        holdingBlockData = material.createBlockData()
        pickupTimeMs = System.currentTimeMillis()

        player.world.playEffect(block.location, Effect.STEP_SOUND, block.type)

        tossRunnable?.cancel()

        var clicked = false

        tossRunnable =
            repeatingTask(1) {
                if (!player.isBlocking) {
                    cancelTossTask()
                    activate()
                }

                if (System.currentTimeMillis() - pickupTimeMs > chargeTimeMs && !clicked) {
                    player.world.playEffect(player.location, Effect.CLICK1, 0)
                    clicked = true
                }
            }
    }

    override fun activate() {
        if (holdingBlockData == null) {
            return
        }

        super.activate()

        val charge = System.currentTimeMillis() - pickupTimeMs
        val multiplier = Math.min(1.4, 1.4 * (charge.toDouble() / chargeTimeMs))

        val fallingBlock =
            player.world.spawn(player.eyeLocation, FallingBlock::class.java).apply {
                blockData = holdingBlockData!!
                setHurtEntities(false)
                dropItem = false
            }

        class BlockEffect() : ProjectileEffect {
            override fun onTick(projectile: BrawlProjectile) {
                val particleCount = Math.min(projectile.projectileEntity?.ticksLived ?: 8, 8)

                Particle.BLOCK_CRUMBLE.builder()
                    .location(
                        projectile.projectileEntity!!
                            .location
                            .addY(projectile.projectileEntity!!.height / 2)
                    )
                    .count(particleCount)
                    .offset(0.1, 0.1, 0.1)
                    .extra(0.1)
                    .data(holdingBlockData!!)
                    .receivers(96, true)
                    .spawn()
            }

            override fun onHit(projectile: BrawlProjectile, hitType: HitType) {
                player.world.playSound(
                    projectile.projectileEntity!!.location,
                    holdingBlockData!!.soundGroup.breakSound,
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
                    .data(holdingBlockData!!)
                    .receivers(96, true)
                    .spawn()
            }
        }

        BrawlProjectile.custom(player, "Block Toss") { fallingBlock }
            .addEffect(BlockEffect())
            .velocityMultiplier(multiplier)
            .onHitEntity { entity, projectile ->
                val blockDamage = Math.min(maxDamage, projectile.velocityBeforeImpact.length() * damage)

                SmashDamageEvent(entity, Damager.DamagerLivingEntity(player), blockDamage).apply {
                    knockbackMultiplier *= this@BlockTossAbility.knockbackMultiplier
                }.callEvent()

                ProjectileAction.DESTROY
            }
            .launch()

        setDisguiseBlock(null)
    }

    private fun setDisguiseBlock(material: Material?) {
        (player.disguise as? EndermanDisguise)?.setHeldBlock(material)
    }

    private fun cancelTossTask() {
        tossRunnable?.cancel()
        tossRunnable = null
    }
}
