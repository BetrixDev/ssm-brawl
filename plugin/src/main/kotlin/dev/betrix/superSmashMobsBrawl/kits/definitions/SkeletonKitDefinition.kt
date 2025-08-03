package dev.betrix.superSmashMobsBrawl.kits.definitions

import dev.betrix.superSmashMobsBrawl.abilities.definitions.BoneExplosionAbilityDefinition
import dev.betrix.superSmashMobsBrawl.abilities.definitions.RopedArrowAbilityDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.SkeletonKitInstance
import dev.betrix.superSmashMobsBrawl.kits.kit
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.ArrowRechargePassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.HungerPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.RegenerationPassiveDefinition
import org.bukkit.entity.Player

object SkeletonKitDefinition : KitDefinition() {
    override val name = "Skeleton"
    override val id = "skeleton"

    override val metadata = kit {
        description = "He got bones"
        meleeDamage = 5

        passive(DoubleJumpPassiveDefinition)
        passive(RegenerationPassiveDefinition)
        passive(HungerPassiveDefinition)
        passive(ArrowRechargePassiveDefinition)

        ability(BoneExplosionAbilityDefinition)
        ability(RopedArrowAbilityDefinition)
    }

    override fun createInstance(
        player: Player,
        minigameDefinition: MinigameDefinition?,
    ): SkeletonKitInstance {
        return SkeletonKitInstance(this, player, minigameDefinition)
    }
}
