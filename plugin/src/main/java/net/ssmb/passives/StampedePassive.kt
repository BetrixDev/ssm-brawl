package net.ssmb.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.events.BrawlDamageEvent
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class StampedePassive(
    private val player: Player,
    private val passiveData: MinigameStartSuccess.PlayerData.KitData.PassiveEntry.PassiveData
) : SsmbPassive(player, passiveData) {
    private val plugin = SSMB.instance

    private var stampedeJob: Job? = null
    private val stackIncreaseTimeMs = getMetaInt("stack_increase_time_ms ", 3000)
    private val maxStacks = getMetaInt("max_stacks", 3)
    private val stopSprintDamage = getMetaDouble("stop_sprint_damage", 3.0)

    private var startTimeMs: Long = 0
    private var stacks = -1
    private var ticks = 0

    override fun initializePassive() {
        super.initializePassive()

        stampedeJob =
            plugin.launch {
                while (true) {
                    delay(1.ticks)

                    if (stacks > 0) {
                        player.world.spawnParticle(Particle.CRIT, player.location, 4)
                    }

                    ticks = (ticks + 1) % 5

                    if (ticks != 0) {
                        continue
                    }

                    if (stacks == -1) {
                        if (player.isSprinting || !player.location.block.isLiquid) {
                            startTimeMs = System.currentTimeMillis()
                            stacks = 0
                        }
                        continue
                    }

                    if (player.isSprinting || player.location.block.isLiquid) {
                        removeStampede()
                        continue
                    }

                    if (stacks > 0) {
                        player.removePotionEffect(PotionEffectType.SPEED)
                        player.addPotionEffect(
                            PotionEffect(PotionEffectType.SPEED, 38, stacks - 1, false, false)
                        )
                    }

                    if (System.currentTimeMillis() - startTimeMs < stackIncreaseTimeMs) {
                        continue
                    }

                    startTimeMs = System.currentTimeMillis()

                    if (stacks < maxStacks) {
                        stacks++
                        player.world.playSound(
                            player.location,
                            Sound.ENTITY_COW_HURT,
                            2f,
                            0.75f + 0.25f * stacks
                        )
                    }
                }
            }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBrawlDamage(event: BrawlDamageEvent) {
        if (event.attacker == player) {
            removeStampede()
        } else if (event.victim == player) {
            if (event.damage < stopSprintDamage || event.isCancelled) {
                return
            }

            removeStampede()
        }
    }

    private fun removeStampede() {
        stacks = -1
        player.removePotionEffect(PotionEffectType.SPEED)
    }

    override fun destroyPassive() {
        super.destroyPassive()
        stampedeJob?.cancel()
    }
}
