package net.ssmb.abilities

import net.ssmb.SSMB
import org.bukkit.entity.Player


class SulphurBombAbility(
    private val player: Player,
    private val plugin: SSMB,
    private val cooldown: Long,
    private val meta: Map<String, String>?
) : SSMBAbility(player, plugin, cooldown, meta) {

    private var lastTimeUsed: Long = 0

    override fun tryActivateAbility() {
        val currentTime = System.currentTimeMillis()

        if (lastTimeUsed + cooldown > currentTime) {
            // Cooldown still active
            return
        }
    }
}