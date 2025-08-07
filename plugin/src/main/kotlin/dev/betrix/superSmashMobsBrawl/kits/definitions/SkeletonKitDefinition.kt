package dev.betrix.superSmashMobsBrawl.kits.definitions

import dev.betrix.superSmashMobsBrawl.abilities.definitions.BoneExplosionAbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.RopedArrowAbilityDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.SkeletonKitInstance
import dev.betrix.superSmashMobsBrawl.kits.kit
import org.bukkit.entity.Player

object SkeletonKitDefinition : KitDefinition() {
    override val name = "Skeleton"
    override val id = "skeleton"

    override val metadata = kit {
        description = "He got bones"
        meleeDamage = 5

        passive(dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition)
        passive(dev.betrix.superSmashMobsBrawl.passives.definitions.RegenerationPassiveDefinition)
        passive(dev.betrix.superSmashMobsBrawl.passives.definitions.HungerPassiveDefinition)
        passive(dev.betrix.superSmashMobsBrawl.passives.definitions.ArrowRechargePassiveDefinition)
        passive(dev.betrix.superSmashMobsBrawl.passives.definitions.BarragePassiveDefinition)

        ability(BoneExplosionAbilityDefinition)
        ability(RopedArrowAbilityDefinition)
    }

    override fun createInstance(
        player: Player,
    ): SkeletonKitInstance {
        return SkeletonKitInstance(this, player)
    }
}
