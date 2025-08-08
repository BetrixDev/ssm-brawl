package dev.betrix.superSmashMobsBrawl.brawl.registry

import dev.betrix.superSmashMobsBrawl.brawl.BrawlPassive
import dev.betrix.superSmashMobsBrawl.brawl.passives.ArrowRechargePassive
import dev.betrix.superSmashMobsBrawl.brawl.passives.BarragePassive
import dev.betrix.superSmashMobsBrawl.brawl.passives.DoubleJumpPassive
import dev.betrix.superSmashMobsBrawl.brawl.passives.HungerPassive
import dev.betrix.superSmashMobsBrawl.brawl.passives.RegenerationPassive
import dev.betrix.superSmashMobsBrawl.passives.PassiveMetadata
import org.bukkit.entity.Player

object BrawlPassiveRegistry {
    private val factories =
        mutableMapOf<String, (Player, PassiveMetadata, Map<String, Any?>) -> BrawlPassive>()

    fun register(
        id: String,
        factory: (Player, PassiveMetadata, Map<String, Any?>) -> BrawlPassive,
    ) {
        factories[id] = factory
    }

    fun create(
        id: String,
        player: Player,
        metadata: PassiveMetadata,
        config: Map<String, Any?> = emptyMap(),
    ): BrawlPassive? {
        val factory = factories[id]
        return factory?.invoke(player, metadata, config)
    }

    init {
        register("double_jump") { player, meta, cfg ->
            DoubleJumpPassive("double_jump", player, meta, cfg)
        }
        register("hunger") { player, meta, cfg -> HungerPassive("hunger", player, meta, cfg) }
        register("regeneration") { player, meta, cfg ->
            RegenerationPassive("regeneration", player, meta, cfg)
        }
        register("arrow_recharge") { player, meta, cfg ->
            ArrowRechargePassive("arrow_recharge", player, meta, cfg)
        }
        register("barrage") { player, meta, cfg -> BarragePassive("barrage", player, meta, cfg) }
    }
}
