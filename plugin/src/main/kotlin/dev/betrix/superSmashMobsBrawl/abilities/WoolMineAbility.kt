package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.disguises.SheepDisguise
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.disguise
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.WoolProjectile
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector

class WoolMineAbility(player: Player) : BrawlAbility("wool_mine", player) {

    private val afterDetonateCooldown = metadata.double("afterDetonateCooldown") ?: 8.0
    private val explosionDamage = metadata.double("explosionDamage") ?: 12.0
    private val damageRadius = metadata.double("damageRadius") ?: 9.0
    private val knockbackMultiplier =
        metadata.double("knockbackMultiplier") ?: 2.0
    private val delayMs = metadata.long("delayMs") ?: 800L
    private val autoDetonateDelayMs = metadata.long("autoDetonateDelayMs") ?: 8000L

    private var projectile: WoolProjectile? = null
    private var woolBlock: Block? = null
    private var originalBlockMaterial: Material? = null
    private var lastDelayTime: Long = 0L
    private var armTime: Long = 0L
    private var blinkTask: TwilightRunnable? = null

    override fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != null && event.hand != EquipmentSlot.HAND) return

        if (!isCorrectActionForUsage(event.action)) {
            return
        }

        val item = event.item ?: return

        if (!isCorrectItemForAbility(item)) {
            return
        }

        event.isCancelled = true

        if (System.currentTimeMillis() - lastDelayTime < delayMs) {
            return
        }

        if (projectile != null) {
            projectile?.solidify(true)
            projectile = null
            return
        }

        if (woolBlock != null) {
            detonate(true)
            return
        }

        if (!canActivate()) {
            return
        }

        launchProjectile()
    }

    private fun launchProjectile() {
        activate()

        projectile =
            WoolProjectile(player) { wool ->
                    onProjectileSolidified(wool)
                }
                .launch() as WoolProjectile

        setSheepSheared(true)
        lastDelayTime = System.currentTimeMillis()
    }

    private fun onProjectileSolidified(wool: WoolProjectile) {
        woolBlock = wool.woolBlock
        originalBlockMaterial = wool.originalBlockMaterial
        armTime = System.currentTimeMillis()

        startBlinkTask()
    }

    private fun startBlinkTask() {
        blinkTask?.cancel()
        blinkTask =
            repeatingTask(0, 5) {
                val block = woolBlock ?: run {
                    cancel()
                    return@repeatingTask
                }

                if (System.currentTimeMillis() - armTime >= autoDetonateDelayMs) {
                    detonate(false)
                    cancel()
                    return@repeatingTask
                }

                if (System.currentTimeMillis() - armTime >= delayMs) {
                    if (block.type == Material.WHITE_WOOL) {
                        block.type = Material.RED_WOOL
                    } else {
                        block.type = Material.WHITE_WOOL
                    }
                }
            }

        blinkTask?.let { runnables.add(it) }
    }

    private fun detonate(inform: Boolean) {
        val block = woolBlock ?: return

        blinkTask?.cancel()
        blinkTask = null

        Particle.EXPLOSION_EMITTER.builder()
            .location(block.location.add(0.5, 0.5, 0.5))
            .count(1)
            .receivers(96, true)
            .spawn()

        block.world.playSound(block.location, Sound.ENTITY_GENERIC_EXPLODE, 3f, 0.8f)

        val explosionCenter = block.location.add(0.5, 0.5, 0.5)

        explosionCenter
            .getNearbyEntities(damageRadius, damageRadius, damageRadius)
            .filterIsInstance<LivingEntity>()
            .filter { canDamageEntity(it) }
            .forEach { entity ->
                val distance = entity.location.distance(explosionCenter)
                val damageMultiplier = 1.0 - (distance / damageRadius).coerceIn(0.0, 1.0)

                BrawlDamageEvent(
                        entity,
                        Damager.DamagerLivingEntity(player),
                        explosionDamage * damageMultiplier + 0.5,
                        0.0,
                        BrawlDamageType.Explosion,
                    )
                    .callEvent()

                val trajectory =
                    entity.location.toVector().subtract(explosionCenter.toVector())
                trajectory.y = 0.0
                trajectory.normalize()

                val velocity = trajectory.multiply(0.5 + 2.5 * damageMultiplier)
                velocity.y = 0.8

                entity.velocity = velocity.apply { y = y.coerceAtMost(10.0) }
            }

        originalBlockMaterial?.let { block.type = it } ?: run { block.type = Material.AIR }

        woolBlock = null
        originalBlockMaterial = null

        if (inform) {
            setCooldown(
                System.currentTimeMillis() - (abilityData.cooldown * 1000L).toLong() +
                    (afterDetonateCooldown * 1000L).toLong()
            )
        }

        setSheepSheared(false)
    }

    private fun canDamageEntity(entity: LivingEntity): Boolean {
        if (entity == player) {
            return false
        }

        if (entity !is Player) {
            return true
        }

        val minigame = minigameService.getMinigameForPlayer(player) ?: return true

        return !minigame.arePlayersOnSameTeam(player, entity)
    }

    private fun setSheepSheared(sheared: Boolean) {
        val disguise = player.disguise as? SheepDisguise ?: return
        if (sheared) {
            disguise.setSheared(true)
        } else if (disguise.isSheared()) {
            disguise.setSheared(false)
            disguise.setColor(DyeColor.WHITE)
        }
    }

    override fun teardown() {
        blinkTask?.cancel()
        blinkTask = null

        woolBlock?.let { block ->
            originalBlockMaterial?.let { block.type = it }
                ?: run { block.type = Material.AIR }
        }
        woolBlock = null

        projectile?.teardown()
        projectile = null

        setSheepSheared(false)

        super.teardown()
    }
}

