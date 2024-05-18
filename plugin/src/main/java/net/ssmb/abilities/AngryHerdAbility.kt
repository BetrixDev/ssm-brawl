package net.ssmb.abilities

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.events.BrawlDamageEvent
import net.ssmb.events.BrawlDamageType
import net.ssmb.extensions.get
import net.ssmb.extensions.metadata
import net.ssmb.utils.TaggedKeyBool
import net.ssmb.utils.didRandomChanceHit
import net.ssmb.utils.isOnGround
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Cow
import org.bukkit.entity.Entity
import org.bukkit.entity.MushroomCow
import org.bukkit.entity.Player
import org.bukkit.util.Vector

// TODO: Refactor to not use ssmos code lol

class AngryHerdAbility(
    private val player: Player,
    abilityEntry: MinigameStartSuccess.PlayerData.KitData.AbilityEntry
) : SsmbAbility(player, abilityEntry) {
    private val plugin = SSMB.instance

    private val abilityCooldown = abilityEntry.ability.cooldown
    private var herdJob: Job? = null

    private val cowDamage = getMetaDouble("cow_damage", 5.0)
    private val cowAmountHeight = getMetaInt("cow_amount_height", 1)
    private val cowAmountRadius = getMetaInt("cow_amount_radius", 5)
    private val stuckTimeMs = getMetaInt("stuck_time_ms", 300)
    private val forceMoveTimeMs = getMetaInt("force_move_time_ms", 350)
    private val damageCooldownMs = getMetaInt("damage_cooldown_ms", 600)
    private val cowHitboxRadius = getMetaDouble("cow_hitbox_radius", 2.2)
    private val knockback = getMetaDouble("knockback", 1.25)
    private val redMooshroomChance = getMetaInt("red_mooshroom_chance", 100)
    private val brownMooshroomChance = getMetaInt("brown_mooshroom_chance", 1000)

    private val cowDirections = hashMapOf<Cow, Vector>()
    private val lastCowLocation = hashMapOf<Cow, Location>()
    private val lastMoveTime = hashMapOf<Cow, Long>()
    private val lastDamageTime = hashMapOf<Entity, Long>()

    override fun doAbility() {
        timeLastUsed = System.currentTimeMillis()

        cowDirections.clear()
        lastCowLocation.clear()
        lastMoveTime.clear()
        lastDamageTime.clear()

        val cows = arrayListOf<Cow>()

        for (j in 0..cowAmountHeight) {
            for (i in 1 - cowAmountRadius..cowAmountRadius) {
                val direction = player.location.direction
                direction.y = 0.0
                direction.normalize()
                val cowLocation = player.location
                cowLocation.add(direction)
                cowLocation.add(Vector(-direction.z, 0.0, direction.x).multiply(i * 1.5))
                cowLocation.add(Vector(0.0, j.toDouble(), 0.0))

                var cow: Cow

                if (didRandomChanceHit(redMooshroomChance)) {
                    cow = player.world.spawn(cowLocation, MushroomCow::class.java)
                    cow.variant = MushroomCow.Variant.RED
                } else if (didRandomChanceHit(brownMooshroomChance)) {
                    cow = player.world.spawn(cowLocation, MushroomCow::class.java)
                    cow.variant = MushroomCow.Variant.BROWN
                } else {
                    cow = player.world.spawn(cowLocation, Cow::class.java)
                }

                cow.metadata {
                    set(TaggedKeyBool("ssmb_entity"), true)
                }

                cows.add(cow)

                val cowDirection = player.location.direction
                cowDirection.y = 0.0
                cowDirection.normalize()
                cowDirection.multiply(0.75)
                cowDirection.y = -0.2

                cowDirections[cow] = cowDirection
                lastCowLocation[cow] = cowLocation
                lastMoveTime[cow] = System.currentTimeMillis()
            }
        }

        player.world.playSound(player.location, Sound.ENTITY_COW_AMBIENT, 2f, 0.6f)

        // TODO: BUG: the job only runs once and doesn't repeat lol

        herdJob =
            plugin.launch {
                if (System.currentTimeMillis() - timeLastUsed > abilityCooldown) {
                    cows.forEach { cow ->
                        if (cow.isValid) {
                            cow.remove()
                        }
                    }
                }

                for (cow in cows) {
                    if (cow.location.distance(lastCowLocation[cow]!!) > 1) {
                        lastCowLocation[cow] = cow.location
                        lastMoveTime[cow] = System.currentTimeMillis()
                    }
                    if (System.currentTimeMillis() - lastMoveTime[cow]!! >= stuckTimeMs) {
                        if (cow.isValid) {
                            cow.world.spawnParticle(
                                Particle.EXPLOSION_NORMAL,
                                cow.location.add(0.0, 1.0, 0.0),
                                1
                            )
                            cow.remove()
                        }
                        continue
                    }
                    if (isOnGround(cow)) {
                        cowDirections[cow] = cowDirections[cow]!!.setY(-0.1)
                    } else {
                        cowDirections[cow] =
                            cowDirections[cow]!!.setY(
                                (-1.0).coerceAtLeast(cowDirections[cow]!!.y - 0.03)
                            )
                    }
                    if (
                        isOnGround(cow) &&
                            System.currentTimeMillis() - lastMoveTime[cow]!! >= forceMoveTimeMs
                    ) {
                        cow.velocity = cowDirections[cow]!!.clone().add(Vector(0.0, 0.75, 0.0))
                    } else {
                        cow.velocity = cowDirections[cow]!!
                    }
                    if (Math.random() > 0.99) {
                        cow.world.playSound(cow.location, Sound.ENTITY_COW_AMBIENT, 1f, 1f)
                    }
                    if (Math.random() > 0.97) {
                        cow.world.playSound(cow.location, Sound.ENTITY_COW_STEP, 1f, 1.2f)
                    }
                    for (plr in player.world.players) {
                        if (plr == player) {
                            continue
                        }
                        if (
                            plr.persistentDataContainer.get(TaggedKeyBool("can_take_damage")) ==
                                false
                        ) {
                            continue
                        }
                        if (cow.location.distance(plr.location) >= cowHitboxRadius) {
                            continue
                        }
                        lastDamageTime.putIfAbsent(player, 0L)
                        if (System.currentTimeMillis() - lastDamageTime[plr]!! < damageCooldownMs) {
                            continue
                        }
                        lastDamageTime[plr] = System.currentTimeMillis()
                        val brawlDamageEvent =
                            BrawlDamageEvent(plr, player, cowDamage, BrawlDamageType.SPECIAL)
                        brawlDamageEvent.knockbackMultiplier = knockback
                        brawlDamageEvent.callEvent()
                        cow.world.spawnParticle(
                            Particle.EXPLOSION_LARGE,
                            cow.location.add(0.0, 1.0, 0.0),
                            1
                        )
                        cow.world.playSound(cow.location, Sound.ENTITY_COW_HURT, 1.5f, 0.75f)
                        cow.world.playSound(
                            cow.location,
                            Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
                            0.75f,
                            0.8f
                        )
                    }
                }

                delay(1.ticks)
            }
    }
}
