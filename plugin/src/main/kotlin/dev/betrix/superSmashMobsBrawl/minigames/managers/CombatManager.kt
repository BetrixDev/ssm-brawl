package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.DeathReason
import dev.betrix.superSmashMobsBrawl.events.PlayerDamageAnalyticsEvent
import dev.betrix.superSmashMobsBrawl.extensions.disguise
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.services.KitService
import gg.flyte.twilight.event.event
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ICombatManager : Listener, IManageable {}

class DefaultCombatManager(private val minigame: BrawlMinigame) :
    Manageable(), ICombatManager, KoinComponent {
    private val kitService: KitService by inject()

    /** 1.8-style melee I-frames (10 ticks) */
    private val meleeIFrame = 500.milliseconds
    private val lastMeleeHitAtByVictim = mutableMapOf<UUID, TimeSource.Monotonic.ValueTimeMark>()

    override fun setup() {
        listeners.add(
            event<PlayerInteractEvent> {
                if (action != Action.LEFT_CLICK_AIR || !isPlayerInMinigame(player)) {
                    return@event
                }

                val playerKit = kitService.getKitForPlayer(player) ?: return@event

                val playerMeleeReach = playerKit.getMeleeReach()
                val playerMeleeDamage = playerKit.getMeleeDamage()

                player.world.livingEntities
                    .filter { entity ->
                        if (entity == player) {
                            return@filter false
                        }

                        // Players on same team should not be targetable
                        if (
                            entity is OfflinePlayer && minigame.arePlayersOnSameTeam(player, entity)
                        ) {
                            return@filter false
                        }

                        val candidateBox =
                            (entity as? Player)?.disguise?.boundingBox ?: entity.boundingBox

                        return@filter isEntityInMeleeReach(player, candidateBox, playerMeleeReach)
                    }
                    .minByOrNull { it.location.distance(player.location) }
                    ?.let {
                        BrawlDamageEvent(
                                it,
                                Damager.DamagerLivingEntity(player),
                                playerMeleeDamage,
                                damageType = BrawlDamageType.MeleeAttack,
                            )
                            .callEvent()
                    }
            }
        )

        listeners.add(
            event<EntityDamageByEntityEvent> {
                if (isCancelled) return@event

                val victimPlayer = entity as? Player ?: return@event

                if (!isPlayerInMinigame(victimPlayer)) return@event

                // Cancel all non-melee damage within this minigame - handled by
                // BrawlDamageEvent
                if (
                    cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                        cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                ) {
                    isCancelled = true
                }
            }
        )

        // Handle SmashDamageEvent - apply damage within the context of this minigame
        listeners.add(
            event<BrawlDamageEvent> {
                // Only handle if this damage concerns players in this minigame
                if (!isPlayerInMinigame(victim as? Player)) return@event

                val victimPlayer = victim as? Player ?: return@event

                // Don't let players on same team damage each other
                ((damager as? Damager.DamagerLivingEntity)?.livingEntity as? Player)?.let {
                    if (minigame.arePlayersOnSameTeam(it, victimPlayer)) {
                        return@event
                    }
                }

                if (victimPlayer.gameMode != GameMode.SURVIVAL) return@event

                // Check if victim is invincible
                val victimKit = kitService.getKitForPlayer(victimPlayer)
                if (victimKit?.isInvincible() == true) {
                    return@event
                }

                val startingHealth = victimPlayer.health
                val newHealth = (startingHealth - damage).coerceAtLeast(0.0)

                // Fire analytics event
                val attackerPlayer =
                    (damager as? Damager.DamagerLivingEntity)?.livingEntity as? Player

                if (attackerPlayer != null && !isPlayerInMinigame(attackerPlayer)) {
                    return@event
                }

                PlayerDamageAnalyticsEvent(
                        victimPlayer,
                        attackerPlayer,
                        damage,
                        minigame,
                        damageType?.toString(),
                    )
                    .callEvent()

                if (newHealth <= 0.0) {
                    // Prevent vanilla death and route through our brawl death flow
                    victimPlayer.health = 1.0
                    BrawlDeathEvent.call(victimPlayer, DeathReason.Damage)
                } else {
                    victimPlayer.health = newHealth
                }

                // Trigger 1.8-style hurtcam/animation and sound on victim
                victimPlayer.playHurtAnimation(1f)
                victimPlayer.playSound(victimPlayer.eyeLocation, Sound.ENTITY_PLAYER_HURT, 1f, 1f)

                // Apply 1.8-style melee knockback only for melee damage (no special damage
                // type)
                if (damageType == null) {
                    val damagerPlayer =
                        (damager as? Damager.DamagerLivingEntity)?.livingEntity as? Player
                    if (damagerPlayer != null) {
                        val kitKnockbackMult =
                            kitService.getKitForPlayer(damagerPlayer)?.getKnockbackMultiplier()
                                ?: 1.0
                        victimPlayer.doKnockback(
                            knockbackMultiplier * kitKnockbackMult,
                            damage,
                            startingHealth,
                            damagerPlayer.location.toVector(),
                            null,
                        )
                        // Start 1.8-style melee invulnerability window
                        if (victimPlayer.maximumNoDamageTicks != 10) {
                            victimPlayer.maximumNoDamageTicks = 10
                        }
                        victimPlayer.noDamageTicks = 10
                        lastMeleeHitAtByVictim[victimPlayer.uniqueId] =
                            TimeSource.Monotonic.markNow()
                    }
                }
            }
        )

        // Translate melee damage into SmashDamageEvent within this minigame
        listeners.add(
            event<EntityDamageByEntityEvent> {
                if (isCancelled) return@event

                val victimPlayer = entity as? Player ?: return@event
                val damagerPlayer = damager as? Player ?: return@event

                if (!isPlayerInMinigame(victimPlayer) || !isPlayerInMinigame(damagerPlayer))
                    return@event

                if (
                    cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                        cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                )
                    return@event

                if (victimPlayer.gameMode != GameMode.SURVIVAL) return@event

                // Cancel vanilla damage and route through SmashDamageEvent using kit melee
                // damage
                isCancelled = true

                // Enforce 1.8-style hit cooldown on victim (10 ticks)
                if (victimPlayer.noDamageTicks > 0) {
                    return@event
                }

                lastMeleeHitAtByVictim[victimPlayer.uniqueId]?.let {
                    if (it.elapsedNow() < meleeIFrame) {
                        return@event
                    }
                }

                val attackerKit = kitService.getKitForPlayer(damagerPlayer)
                val meleeDamage = attackerKit?.getMeleeDamage() ?: damage

                BrawlDamageEvent(
                        victimPlayer,
                        Damager.DamagerLivingEntity(damagerPlayer),
                        meleeDamage,
                    )
                    .callEvent()
            }
        )
    }

    private fun isPlayerInMinigame(player: Player?): Boolean {
        return player != null &&
            minigame.allPlayers().any { it.isOnline && it.player?.uniqueId == player.uniqueId } &&
            !minigame.connectionManager.isDisconnected(player)
    }

    override fun teardown() {
        super.teardown()
        lastMeleeHitAtByVictim.clear()
    }

    private fun isEntityInMeleeReach(
        player: Player,
        boundingBox: BoundingBox,
        meleeReach: Double,
    ): Boolean {
        val eyeLocation = player.eyeLocation
        val direction = eyeLocation.direction

        // Cast ray from player's eye location in their looking direction
        val rayStart = eyeLocation.toVector()

        // Check intersection with bounding box
        val intersection = rayIntersectsBoundingBox(rayStart, direction, boundingBox)

        return intersection != null && intersection.distance(rayStart) <= meleeReach
    }

    private fun rayIntersectsBoundingBox(
        rayStart: Vector,
        rayDirection: Vector,
        boundingBox: BoundingBox,
    ): Vector? {
        val min = Vector(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
        val max = Vector(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)

        var tMin =
            if (rayDirection.x != 0.0) (min.x - rayStart.x) / rayDirection.x
            else Double.NEGATIVE_INFINITY
        var tMax =
            if (rayDirection.x != 0.0) (max.x - rayStart.x) / rayDirection.x
            else Double.POSITIVE_INFINITY

        if (tMin > tMax) {
            val temp = tMin
            tMin = tMax
            tMax = temp
        }

        var tyMin =
            if (rayDirection.y != 0.0) (min.y - rayStart.y) / rayDirection.y
            else Double.NEGATIVE_INFINITY
        var tyMax =
            if (rayDirection.y != 0.0) (max.y - rayStart.y) / rayDirection.y
            else Double.POSITIVE_INFINITY

        if (tyMin > tyMax) {
            val temp = tyMin
            tyMin = tyMax
            tyMax = temp
        }

        if (tMin > tyMax || tyMin > tMax) {
            return null
        }

        if (tyMin > tMin) tMin = tyMin
        if (tyMax < tMax) tMax = tyMax

        var tzMin =
            if (rayDirection.z != 0.0) (min.z - rayStart.z) / rayDirection.z
            else Double.NEGATIVE_INFINITY
        var tzMax =
            if (rayDirection.z != 0.0) (max.z - rayStart.z) / rayDirection.z
            else Double.POSITIVE_INFINITY

        if (tzMin > tzMax) {
            val temp = tzMin
            tzMin = tzMax
            tzMax = temp
        }

        if (tMin > tzMax || tzMin > tMax) {
            return null
        }

        if (tzMin > tMin) tMin = tzMin

        // Return intersection point
        return if (tMin >= 0) {
            rayStart.clone().add(rayDirection.clone().multiply(tMin))
        } else null
    }
}
