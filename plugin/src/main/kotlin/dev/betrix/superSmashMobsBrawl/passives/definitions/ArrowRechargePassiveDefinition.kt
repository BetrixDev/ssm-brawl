package dev.betrix.superSmashMobsBrawl.passives.definitions

import dev.betrix.superSmashMobsBrawl.passives.instances.ArrowRechargePassiveInstance
import dev.betrix.superSmashMobsBrawl.passives.passive
import org.bukkit.entity.Player

object ArrowRechargePassiveDefinition : PassiveDefinition() {
    override val name = "Arrow Recharge"
    override val id = "arrow_recharge"

    override val metadata = passive {
        description = "Recharges your arrows"
        userFacing = true
    }

    override fun createInstance(player: Player): ArrowRechargePassiveInstance {
        return ArrowRechargePassiveInstance(this, player)
    }
}