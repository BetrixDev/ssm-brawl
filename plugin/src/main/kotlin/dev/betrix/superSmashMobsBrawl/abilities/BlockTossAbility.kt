package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.disguises.EndermanDisguise
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.extensions.disguise
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileAction
import dev.betrix.superSmashMobsBrawl.projectiles.effects.BlockEffect
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.*
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import org.joml.Matrix4f

class BlockTossAbility(player: Player) : BrawlAbility("block_toss", player) {

    private var holdingBlockData: BlockData? = null
    private var pickupTimeMs: Long = 0
    private var tossRunnable: TwilightRunnable? = null
    private val chargeTimeMs = metadata.long("chargeTimeMs") ?: 1200L
    private val maxCharge = metadata.double("maxCharge") ?: 1.4
    private val maxDamage = metadata.double("maxDamage") ?: 9.0
    private val damage = metadata.double("damage") ?: 8.0
    private val knockbackMultiplier = metadata.double("knockbackMultiplier") ?: 2.5

    private val activeProjectiles = mutableListOf<BrawlProjectile>()

    override fun setup() {
        super.setup()
    }

    override fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != null && event.hand != EquipmentSlot.HAND) return

        event.isCancelled = true

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

        val currentChargeTime = System.currentTimeMillis() - pickupTimeMs
        val chargeValue =
            Math.min(maxCharge, maxCharge * (currentChargeTime.toDouble() / chargeTimeMs))
                .coerceAtLeast(0.2)
        val chargePercent = chargeValue / maxCharge

        val displayBlock =
            player.world.spawn(player.eyeLocation, BlockDisplay::class.java).apply {
                block = holdingBlockData!!
            }

        val fallingBlock =
            player.world.spawn(player.eyeLocation, Snowball::class.java).apply {
                isInvisible = true
                isInvulnerable = true
                velocity = Vector()
                addPassenger(displayBlock)
            }

        val projectile =
            BrawlProjectile.custom(player, "abilities.$id.name") { fallingBlock }
                .addEffect(BlockEffect(holdingBlockData!!))
                .velocityMultiplier(chargeValue)
                .onHitEntity { entity, projectile ->
                    val blockDamage =
                        Math.min(maxDamage, projectile.velocityBeforeImpact.length() * damage)

                    BrawlDamageEvent(entity, Damager.DamagerLivingEntity(player), blockDamage)
                        .apply { knockbackMultiplier *= this@BlockTossAbility.knockbackMultiplier }
                        .callEvent()

                    ProjectileAction.DESTROY
                }
                .onTeardown { activeProjectiles.remove(it) }
                .launch()

        activeProjectiles.add(projectile)

        val scaleFactor =
            (0.5 + (1.1 - 0.5) * chargePercent)
                .toFloat() // Lerp between 0.5 and 1.1 based on charge percent for block scale
        var tumbleAngleY = 0f
        var tumbleAngleX = 0f

        val rotatationStepSpeedTicks = 4

        runnables.add(
            repeatingTask(rotatationStepSpeedTicks.toLong()) {
                if (!displayBlock.isValid) {
                    cancel()
                    return@repeatingTask
                }

                if (projectile.projectileEntity?.isValid != true) {
                    displayBlock.remove()
                    cancel()
                    return@repeatingTask
                }

                tumbleAngleY += 0.40f
                tumbleAngleX += 0.30f

                val transform =
                    Matrix4f()
                        .rotateY(tumbleAngleY)
                        .rotateX(tumbleAngleX)
                        .scale(scaleFactor)
                        .translate(-0.5f, -0.5f, -0.5f)

                displayBlock.setTransformationMatrix(transform)
                displayBlock.interpolationDelay = 0
                displayBlock.interpolationDuration = rotatationStepSpeedTicks
            }
        )

        setDisguiseBlock(null)
        holdingBlockData = null
    }

    override fun teardown() {
        cancelTossTask()
        setDisguiseBlock(null)
        activeProjectiles.forEach { it.teardown() }
        activeProjectiles.clear()
        super.teardown()
    }

    private fun setDisguiseBlock(material: Material?) {
        (player.disguise as? EndermanDisguise)?.setHeldBlock(material)
    }

    private fun cancelTossTask() {
        tossRunnable?.cancel()
        tossRunnable = null
    }
}
