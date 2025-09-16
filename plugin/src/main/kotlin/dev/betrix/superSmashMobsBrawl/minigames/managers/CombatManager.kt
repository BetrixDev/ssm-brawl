package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.DeathReason
import dev.betrix.superSmashMobsBrawl.events.PlayerDamageAnalyticsEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.KitService
import gg.flyte.twilight.event.event
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ICombatManager : Listener {
    fun initialize(minigame: BrawlMinigame)
}

class DefaultCombatManager : Manageable(), ICombatManager, KoinComponent {
    private val kitService: KitService by inject()
    private val dataService: DataService by inject()
    private val plugin: SuperSmashMobsBrawl by inject()

    /** 1.8-style melee I-frames (10 ticks) */
    private val meleeIFrame = 500.milliseconds
    private val lastMeleeHitAtByVictim = mutableMapOf<UUID, TimeSource.Monotonic.ValueTimeMark>()

    private lateinit var minigame: BrawlMinigame

    override fun initialize(minigame: BrawlMinigame) {
        this.minigame = minigame

        // Handle SmashDamageEvent - apply damage within the context of this minigame
        listeners.add(
            event<SmashDamageEvent> {
                // Only handle if this damage concerns players in this minigame
                if (!isPlayerInMinigame(victim as? Player)) return@event

                val victimPlayer = victim as? Player ?: return@event

                if (victimPlayer.gameMode != GameMode.SURVIVAL) return@event

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

                // Apply 1.8-style melee knockback only for melee damage (no special damage type)
                if (damageType == null) {
                    val damagerPlayer =
                        (damager as? Damager.DamagerLivingEntity)?.livingEntity as? Player
                    if (damagerPlayer != null) {
                        val kitKnockbackMult =
                            kitService.getKitForPlayer(damagerPlayer)?.let {
                                dataService.getKit(it.id)?.knockbackMultiplier
                            } ?: 1.0
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

                // Cancel vanilla damage and route through SmashDamageEvent using kit melee damage
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

                SmashDamageEvent(
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
}
