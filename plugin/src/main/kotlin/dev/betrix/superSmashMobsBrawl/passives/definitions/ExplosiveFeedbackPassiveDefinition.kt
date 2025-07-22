package dev.betrix.superSmashMobsBrawl.passives.definitions

import dev.betrix.superSmashMobsBrawl.passives.instances.ExplosiveFeedbackInstance
import dev.betrix.superSmashMobsBrawl.passives.instances.PassiveInstance
import dev.betrix.superSmashMobsBrawl.passives.passive
import org.bukkit.entity.Player

object ExplosiveFeedbackPassiveDefinition : PassiveDefinition() {
    override val name = "Explosive Feedback"
    override val id = "explosive_feedback"

    override val metadata = passive {
        description = "When taking damage, create small explosions that damage nearby enemies"
        userFacing = true
    }

    override fun createInstance(player: Player): ExplosiveFeedbackInstance {
        return ExplosiveFeedbackInstance(this, player)
    }
}