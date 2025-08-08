package dev.betrix.superSmashMobsBrawl.brawl.passives

import dev.betrix.superSmashMobsBrawl.brawl.BrawlPassive
import dev.betrix.superSmashMobsBrawl.passives.PassiveMetadata
import org.bukkit.entity.Player

class ArrowRechargePassive(
    id: String,
    player: Player,
    metadata: PassiveMetadata,
    config: Map<String, Any?> = emptyMap(),
) : BrawlPassive(id, player, metadata, config) {

    private var legacy:
        dev.betrix.superSmashMobsBrawl.passives.instances.ArrowRechargePassiveInstance? =
        null

    override fun setup() {
        legacy =
            dev.betrix.superSmashMobsBrawl.passives.definitions.ArrowRechargePassiveDefinition
                .createInstance(player)
        legacy?.setup()
        super.setup()
    }

    override fun teardown() {
        legacy?.teardown()
        legacy = null
        super.teardown()
    }
}
