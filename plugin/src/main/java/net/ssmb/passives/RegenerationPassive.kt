package net.ssmb.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlin.math.min
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

class RegenerationPassive(
    private val player: Player,
    passiveData: MinigameStartSuccess.PlayerData.KitData.PassiveEntry.PassiveData
) : SsmbPassive(player, passiveData) {
    private val plugin = SSMB.instance
    private val regenDelayTicks = getMetaInt("regen_delay_ticks", 10)
    private val regenRate = getMetaInt("regen_rate", 20)

    private var regenerationJob: Job? = null

    override fun initializePassive() {
        regenerationJob =
            plugin.launch {
                while (true) {
                    if (player.isDead) continue
                    if (player.foodLevel <= 0) continue
                    if (player.health != player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value)
                        continue

                    player.health = min(player.health + regenRate, 20.0)
                    player.sendHealthUpdate()

                    delay(regenDelayTicks.ticks)
                }
            }
    }

    override fun destroyPassive() {
        regenerationJob?.cancel()
    }
}
