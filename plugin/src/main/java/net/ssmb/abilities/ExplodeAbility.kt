package net.ssmb.abilities

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.extensions.*
import net.ssmb.utils.TaggedKeyBool
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerToggleSneakEvent

class ExplodeAbility(
    private val player: Player,
    abilityEntry: MinigameStartSuccess.PlayerData.KitData.AbilityEntry
) : SsmbAbility(player, abilityEntry) {
    private val plugin = SSMB.instance

    private var isExplodeActive = false

    override fun doAbility() {
        val currentTime = System.currentTimeMillis()

        isExplodeActive = true

        plugin.launch {
            player.walkSpeed = 0.05f
            player.level = 0
            player.exp = 0f

            repeat(30) { index ->
                if (isExplodeActive) {
                    player.exp = (index + 1) / 30f

                    val volume = 0.5f + index / 20
                    player.world.playSound(
                        player.location,
                        Sound.ENTITY_CREEPER_PRIMED,
                        volume,
                        volume
                    )

                    delay(1.ticks)
                } else {
                    return@launch
                }
            }

            player.walkSpeed = 0.2f
            player.level = 0
            player.exp = 0f

            if (isExplodeActive) {
                isExplodeActive = false

                player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
                player.world.spawnParticle(Particle.EXPLOSION_LARGE, player.location, 3)

                player.getLivingEntitiesInRadius(8.0).forEach {
                    if (
                        it == player ||
                            it.getMetadata((TaggedKeyBool("player_can_be_damaged"))) == false
                    ) {
                        return@forEach
                    }

                    val distance = player.location.distance(it.location)
                    val damage = (0.1 + 0.9 * ((8 - distance) / 8)) * 0.75

                    it.doKnockback(2.5, damage, it.health, player.location.toVector(), null)
                    it.damage(damage, player)
                }

                timeLastUsed = System.currentTimeMillis()
            }
        }
    }

    @EventHandler
    fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        if (event.player != player || !isExplodeActive) return

        isExplodeActive = false
        player.walkSpeed = 0.2F
        player.level = 0
        player.exp = 0F
    }
}
