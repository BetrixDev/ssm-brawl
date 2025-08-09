package dev.betrix.superSmashMobsBrawl.brawl.registries

import dev.betrix.superSmashMobsBrawl.abilities.AbilityMetadata
import dev.betrix.superSmashMobsBrawl.brawl.BrawlAbility
import dev.betrix.superSmashMobsBrawl.brawl.abilities.BoneExplosionAbility
import dev.betrix.superSmashMobsBrawl.brawl.abilities.ExplosionAbility
import dev.betrix.superSmashMobsBrawl.brawl.abilities.RopedArrowAbility
import dev.betrix.superSmashMobsBrawl.brawl.abilities.SulphurBombAbility
import org.bukkit.entity.Player

object BrawlAbilityRegistry {
    private val factories = mutableMapOf<String, (Player, AbilityMetadata) -> BrawlAbility>()

    fun register(id: String, factory: (Player, AbilityMetadata) -> BrawlAbility) {
        factories[id] = factory
    }

    fun create(id: String, player: Player, metadata: AbilityMetadata): BrawlAbility? {
        val factory = factories[id]
        return factory?.invoke(player, metadata)
    }

    init {
        register("sulphur_bomb") { player, meta ->
            SulphurBombAbility("sulphur_bomb", player, meta)
        }
        register("bone_explosion") { player, meta ->
            BoneExplosionAbility("bone_explosion", player, meta)
        }
        register("roped_arrow") { player, meta -> RopedArrowAbility("roped_arrow", player, meta) }
        // Backwards compatibility mapping for data typo
        register("explode") { player, meta -> ExplosionAbility("explosion", player, meta) }
    }
}
